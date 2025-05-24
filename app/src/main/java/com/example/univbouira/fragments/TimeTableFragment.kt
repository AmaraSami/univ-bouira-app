package com.example.univbouira.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.univbouira.R
import com.example.univbouira.models.StudentSlot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import com.google.firebase.auth.FirebaseAuth


class TimeTableFragment : Fragment(R.layout.fragment_time_table) {
    private lateinit var tableLayout: TableLayout
    private lateinit var emptyView: TextView
    private lateinit var semester1Button: Button
    private lateinit var semester2Button: Button

    private val timeSlots = mutableListOf<StudentSlot>()
    private var selectedSemester = 1

    private val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")
    private val times =
        listOf("08:00-09:30", "09:30-11:00", "11:00-12:30", "12:30-14:00", "14:00-15:30")

    private val db = FirebaseFirestore.getInstance()

    private var studentLevel: String? = null
    private var studentGroup: String? = null


    companion object {
        private const val TAG = "TimeTableFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableLayout = view.findViewById(R.id.tableLayout)
        emptyView = view.findViewById(R.id.empty_view)
        semester1Button = view.findViewById(R.id.semester1Button)
        semester2Button = view.findViewById(R.id.semester2Button)

        semester1Button.setOnClickListener {
            if (selectedSemester != 1) {
                selectedSemester = 1
                updateButtonColors()
                if (studentLevel != null && studentGroup != null) {
                    loadTimeTableData()
                }
            }
        }
        semester2Button.setOnClickListener {
            if (selectedSemester != 2) {
                selectedSemester = 2
                updateButtonColors()
                if (studentLevel != null && studentGroup != null) {
                    loadTimeTableData()
                }
            }
        }

        updateButtonColors()
        fetchStudentInfoAndLoadTimetable()
    }

    private fun fetchStudentInfoAndLoadTimetable() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        if (userEmail.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("students")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "Student not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val studentData = result.documents[0].data
                studentLevel = studentData?.get("level") as? String
                studentGroup = studentData?.get("groupName") as? String

                if (studentLevel != null && studentGroup != null) {
                    Log.d(TAG, "Fetched student level=$studentLevel, group=$studentGroup")
                    loadTimeTableData()
                } else {
                    Toast.makeText(requireContext(), "Missing student level/group", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching student info", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun updateButtonColors() {
        val selectedColor = Color.parseColor("#007BA7")
        val defaultColor = Color.parseColor("#BDBDBD")
        semester1Button.setBackgroundColor(if (selectedSemester == 1) selectedColor else defaultColor)
        semester2Button.setBackgroundColor(if (selectedSemester == 2) selectedColor else defaultColor)
    }

    private fun loadTimeTableData() {
        timeSlots.clear()
        tableLayout.removeAllViews()

        val sem = "Semester $selectedSemester"
        Log.d(TAG, "Loading timetable for $sem / $studentLevel / $studentGroup")

        val slotsRef = db.collection("studentTimetables")
            .document(sem)
            .collection("levels")
            .document(studentLevel!!)
            .collection("groups")
            .document(studentGroup!!)
            .collection("timeSlots")

        slotsRef.get()
            .addOnSuccessListener { snap ->
                val fetchedSlots = mutableListOf<StudentSlot>()
                val pendingLookups = mutableListOf<Pair<String, Int>>()

                snap.documents.forEachIndexed { index, doc ->
                    val data = doc.data ?: return@forEachIndexed
                    val day = data["day"] as? String ?: ""
                    val time = data["time"] as? String ?: ""
                    val course = data["courseCode"] as? String ?: data["subject"] as? String ?: ""
                    val room = data["room"] as? String ?: ""
                    val type = data["type"] as? String ?: ""
                    val teacherId = data["teacherId"] as? String
                    var teacherName = data["teacherName"] as? String ?: ""

                    if (teacherName.isEmpty() && !teacherId.isNullOrEmpty()) {
                        fetchedSlots.add(StudentSlot(day, time, course, room, "", type))
                        pendingLookups.add(Pair(teacherId, index))
                    } else {
                        fetchedSlots.add(StudentSlot(day, time, course, room, teacherName, type))
                    }
                }

                if (pendingLookups.isEmpty()) {
                    timeSlots.addAll(fetchedSlots)
                    renderTable()
                } else {
                    val lookupsDone = BooleanArray(pendingLookups.size) { false }

                    pendingLookups.forEachIndexed { i, (tid, idx) ->
                        db.collection("instructors").document(tid).get()
                            .addOnSuccessListener { doc ->
                                val name = doc.getString("fullName") ?: ""
                                fetchedSlots[idx] = fetchedSlots[idx].copy(teacherId = name)
                                lookupsDone[i] = true
                                if (lookupsDone.all { it }) {
                                    timeSlots.addAll(fetchedSlots)
                                    renderTable()
                                }
                            }.addOnFailureListener {
                                lookupsDone[i] = true
                                if (lookupsDone.all { it }) {
                                    timeSlots.addAll(fetchedSlots)
                                    renderTable()
                                }
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading timetable", e)
                Toast.makeText(requireContext(), "Failed to load: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun renderTable() {
        if (timeSlots.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            tableLayout.visibility = View.GONE
            Toast.makeText(requireContext(), "No timetable data available", Toast.LENGTH_SHORT)
                .show()
            return
        }

        emptyView.visibility = View.GONE
        tableLayout.visibility = View.VISIBLE

        val scale = resources.displayMetrics.density
        val cellWidthPx = (120 * scale).toInt()
        val paddingPx = (4 * scale).toInt()
        val minHeightPx = (80 * scale).toInt()

        val todayName = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            else -> ""
        }

        for (time in times) {
            val row = TableRow(requireContext()).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                setShowDividers(LinearLayout.SHOW_DIVIDER_NONE)
            }

            TextView(requireContext()).apply {
                text = time
                setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                gravity = Gravity.CENTER
                minHeight = minHeightPx
                textSize = 12f
                layoutParams =
                    TableRow.LayoutParams(cellWidthPx, TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundResource(R.drawable.cell_border)
            }.also { row.addView(it) }

            for (day in days) {
                TextView(requireContext()).apply {
                    setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                    gravity = Gravity.CENTER
                    minHeight = minHeightPx
                    textSize = 12f
                    maxLines = 4
                    ellipsize = TextUtils.TruncateAt.END
                    layoutParams =
                        TableRow.LayoutParams(cellWidthPx, TableRow.LayoutParams.MATCH_PARENT)

                    val slot = timeSlots.find { it.day == day && it.time == time }
                    if (slot != null) {
                        text = "${slot.type}\n${slot.courseCode}\n${slot.room}\n${slot.teacherId}"
                        when (slot.type.lowercase()) {
                            "cour" -> setBackgroundResource(R.drawable.cour_cell_background)
                            "td" -> setBackgroundResource(R.drawable.td_cell_background)
                            "tp" -> setBackgroundResource(R.drawable.tp_cell_background)
                        }
                        if (day.equals(todayName, true)) {
                            ValueAnimator.ofObject(
                                ArgbEvaluator(),
                                Color.TRANSPARENT,
                                Color.parseColor("#9AF0FF")
                            ).apply {
                                duration = 1000
                                addUpdateListener { setBackgroundColor(it.animatedValue as Int) }
                                start()
                            }
                        }
                    } else {
                        setBackgroundResource(R.drawable.cell_border)
                        text = ""
                    }
                }.also { row.addView(it) }
            }

            tableLayout.addView(row)
        }
    }
}