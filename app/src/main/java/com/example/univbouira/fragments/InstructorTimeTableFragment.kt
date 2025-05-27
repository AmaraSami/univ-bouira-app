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
import com.example.univbouira.models.GroupItem
import com.example.univbouira.models.InstructorSlot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class InstructorTimeTableFragment : Fragment(R.layout.fragment_time_table) {

    private lateinit var tableLayout: TableLayout
    private lateinit var emptyView: TextView
    private lateinit var semester1Button: Button
    private lateinit var semester2Button: Button

    private val timeSlots = mutableListOf<InstructorSlot>()
    private var selectedSemester = 1

    private val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")
    private val times = listOf("08:00-09:30", "09:30-11:00", "11:00-12:30", "12:30-14:00", "14:00-15:30")

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "InstructorTimeTable"
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
                loadTimeTableData()
            }
        }
        semester2Button.setOnClickListener {
            if (selectedSemester != 2) {
                selectedSemester = 2
                updateButtonColors()
                loadTimeTableData()
            }
        }

        updateButtonColors()
        loadTimeTableData()
    }

    private fun updateButtonColors() {
        val selectedColor = Color.parseColor("#007BA7")
        val defaultColor = Color.parseColor("#BDBDBD")
        semester1Button.setBackgroundColor(if (selectedSemester == 1) selectedColor else defaultColor)
        semester2Button.setBackgroundColor(if (selectedSemester == 2) selectedColor else defaultColor)
    }

    private fun loadTimeTableData() {
        val currentUser = auth.currentUser ?: return
        val instructorId = currentUser.uid

        val sem = "Semester$selectedSemester"

        timeSlots.clear()
        if (isAdded) {
            tableLayout.removeAllViews()
        }

        Log.d(TAG, "🔍 DEBUG: Looking for instructor: $instructorId, semester: $sem")

        db.collection("instructorTimetables")
            .document(instructorId)
            .collection("timeSlots")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                Log.d(TAG, "🔍 DEBUG: Found ${snapshot.size()} total timeslot documents")

                timeSlots.clear()

                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue
                    val semester = data["semester"] as? String ?: ""

                    Log.d(TAG, "🔍 DEBUG: Document ${doc.id}: semester='$semester', looking for '$sem'")

                    if (semester != sem) {
                        Log.d(TAG, "🔍 DEBUG: Skipping document ${doc.id} - wrong semester")
                        continue
                    }

                    val day = data["day"] as? String ?: ""
                    val time = data["time"] as? String ?: ""
                    val courseCode = doc.id
                    val room = data["room"] as? String ?: ""
                    val type = data["type"] as? String ?: ""
                    val levels = data["levels"] as? List<String> ?: listOf()
                    val groups = data["groups"] as? List<String> ?: listOf()

                    Log.d(TAG, "🔍 DEBUG: Adding slot: day='$day', time='$time', course='$courseCode', room='$room', type='$type'")

                    timeSlots.add(
                        InstructorSlot(
                            semester = semester,
                            day = day,
                            time = time,
                            courseCode = courseCode,
                            room = room,
                            type = type,
                            levels = levels,
                            groups = groups
                        )
                    )
                }

                Log.d(TAG, "🔍 DEBUG: Final timeSlots count: ${timeSlots.size}")
                Log.d(TAG, "🔍 DEBUG: Available slots: ${timeSlots.map { "${it.day}_${it.time}" }}")

                if (timeSlots.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    tableLayout.visibility = View.GONE
                    Toast.makeText(requireContext(), "No timetable data available", Toast.LENGTH_SHORT).show()
                } else {
                    emptyView.visibility = View.GONE
                    tableLayout.visibility = View.VISIBLE
                    buildTable()
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Log.e(TAG, "Error loading timetable", e)
                Toast.makeText(requireContext(), "Failed to load: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun buildTable() {
        if (!isAdded) return

        // Clear any existing rows
        tableLayout.removeAllViews()

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

        Log.d(TAG, "🔍 DEBUG: Building table for ${times.size} time slots and ${days.size} days")
        Log.d(TAG, "🔍 DEBUG: Today is: $todayName")

        for (time in times) {
            val row = TableRow(requireContext()).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                setShowDividers(LinearLayout.SHOW_DIVIDER_NONE)
            }

            // Time column
            TextView(requireContext()).apply {
                text = time
                setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                gravity = Gravity.CENTER
                minHeight = minHeightPx
                textSize = 12f
                layoutParams = TableRow.LayoutParams(cellWidthPx, TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundResource(R.drawable.cell_border)
            }.also { row.addView(it) }

            // Day columns
            for (day in days) {
                val slot = timeSlots.find {
                    it.day.equals(day, ignoreCase = true) && it.time == time
                }

                Log.d(TAG, "🔍 DEBUG: Looking for slot $day $time - Found: ${slot != null}")
                if (slot != null) {
                    Log.d(TAG, "🔍 DEBUG: Slot details: ${slot.courseCode}, ${slot.room}, ${slot.type}")
                }

                TextView(requireContext()).apply {
                    setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                    gravity = Gravity.CENTER
                    minHeight = minHeightPx
                    textSize = 10f
                    maxLines = 5
                    ellipsize = TextUtils.TruncateAt.END
                    layoutParams = TableRow.LayoutParams(cellWidthPx, TableRow.LayoutParams.MATCH_PARENT)

                    if (slot != null) {
                        val levelText = slot.levels.joinToString(", ") { it.uppercase() }

                        val groupText = try {
                            slot.groups.joinToString(", ") { groupId ->
                                if (groupId.contains("_")) {
                                    GroupItem.fromDocumentId(groupId).getShortName()
                                } else {
                                    groupId.uppercase()
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error formatting groups: ${slot.groups}", e)
                            slot.groups.joinToString(", ")
                        }

                        val levelGroupInfo = when {
                            levelText.isNotBlank() && groupText.isNotBlank() -> "$levelText | $groupText"
                            levelText.isNotBlank() -> levelText
                            groupText.isNotBlank() -> groupText
                            else -> ""
                        }

                        text = "${slot.type.uppercase()}\n${slot.courseCode}\n${slot.room}\n$levelGroupInfo"

                        Log.d(TAG, "🔍 DEBUG: Setting cell text for $day $time: '$text'")

                        when (slot.type.lowercase()) {
                            "cour" -> setBackgroundResource(R.drawable.cour_cell_background)
                            "td" -> setBackgroundResource(R.drawable.td_cell_background)
                            "tp" -> setBackgroundResource(R.drawable.tp_cell_background)
                            else -> setBackgroundResource(R.drawable.cell_border)
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

                        Log.d(TAG, "✅ PLACED: $day $time -> ${slot.courseCode}")
                    } else {
                        setBackgroundResource(R.drawable.cell_border)
                        text = ""
                        Log.d(TAG, "❌ EMPTY: $day $time")
                    }
                }.also { row.addView(it) }
            }

            tableLayout.addView(row)
        }

        Log.d(TAG, "🔍 DEBUG: Table built with ${tableLayout.childCount} rows")
    }
}