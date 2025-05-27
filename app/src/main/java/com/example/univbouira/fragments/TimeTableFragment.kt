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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar

class TimeTableFragment : Fragment(R.layout.fragment_time_table) {

    // UI Components
    private lateinit var tableLayout: TableLayout
    private lateinit var emptyView: TextView
    private lateinit var semester1Button: Button
    private lateinit var semester2Button: Button

    // Data
    private val timeSlots = mutableListOf<StudentSlot>()
    private var selectedSemester = 1
    private var studentLevel: String? = null
    private var studentGroup: String? = null

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var timetableListener: ListenerRegistration? = null

    // Constants
    private val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")
    private val times = listOf("08:00-09:30", "09:30-11:00", "11:00-12:30", "12:30-14:00", "14:00-15:30")

    companion object {
        private const val TAG = "TimeTableFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        loadStudentData()
    }

    private fun initializeViews(view: View) {
        tableLayout = view.findViewById(R.id.tableLayout)
        emptyView = view.findViewById(R.id.empty_view)
        semester1Button = view.findViewById(R.id.semester1Button)
        semester2Button = view.findViewById(R.id.semester2Button)

        updateButtonColors()
    }

    private fun setupClickListeners() {
        semester1Button.setOnClickListener {
            if (selectedSemester != 1) {
                selectedSemester = 1
                updateButtonColors()
                loadTimetableData()
            }
        }

        semester2Button.setOnClickListener {
            if (selectedSemester != 2) {
                selectedSemester = 2
                updateButtonColors()
                loadTimetableData()
            }
        }
    }

    private fun updateButtonColors() {
        val selectedColor = Color.parseColor("#007BA7")
        val defaultColor = Color.parseColor("#BDBDBD")

        semester1Button.setBackgroundColor(if (selectedSemester == 1) selectedColor else defaultColor)
        semester2Button.setBackgroundColor(if (selectedSemester == 2) selectedColor else defaultColor)
    }

    private fun showError(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            Log.e(TAG, message)
        }
    }

    private fun loadStudentData() {
        val userEmail = auth.currentUser?.email

        if (userEmail.isNullOrEmpty()) {
            showError("User not logged in")
            return
        }

        db.collection("students")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener

                if (result.isEmpty) {
                    showError("Student profile not found")
                    return@addOnSuccessListener
                }

                try {
                    val studentData = result.documents[0].data
                    studentLevel = studentData?.get("level") as? String
                    var groupName = studentData?.get("groupName") as? String

                    // Handle legacy group format conversion
                    if (groupName != null && !groupName.contains("_") && studentLevel != null) {
                        groupName = "${studentLevel}_${groupName.replace(" ", "_")}"
                        Log.d(TAG, "Converted groupName to new format: $groupName")
                    }

                    studentGroup = groupName

                    if (studentLevel != null && studentGroup != null) {
                        Log.d(TAG, "Student loaded: level=$studentLevel, group=$studentGroup")
                        loadTimetableData()
                    } else {
                        showError("Missing student level or group information")
                        showEmptyState("Student profile incomplete")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing student data", e)
                    showError("Error loading student information")
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Log.e(TAG, "Error fetching student data", e)
                showError("Failed to load student data: ${e.message}")
            }
    }

    private fun loadTimetableData() {
        if (studentLevel == null || studentGroup == null) {
            showError("Student information not loaded")
            return
        }

        // Remove previous listener
        timetableListener?.remove()

        timeSlots.clear()
        clearTable()

        val sem = "Semester$selectedSemester"
        Log.d(TAG, "Loading timetable for $sem / $studentLevel / $studentGroup")

        val slotsRef = db.collection("studentTimetables")
            .document(sem)
            .collection("levels")
            .document(studentLevel!!)
            .collection("groups")
            .document(studentGroup!!)
            .collection("timeSlots")

        // Use real-time listener for live updates
        timetableListener = slotsRef.addSnapshotListener { snapshot, e ->
            if (!isAdded) return@addSnapshotListener

            if (e != null) {
                Log.e(TAG, "Error listening to timetable changes", e)
                showError("Failed to load timetable: ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                processTimetableData(snapshot.documents.mapNotNull { it.data })
            } else {
                Log.d(TAG, "No timetable data found")
                timeSlots.clear()
                renderTable()
            }
        }
    }

    private fun processTimetableData(documents: List<Map<String, Any>>) {
        val fetchedSlots = mutableListOf<StudentSlot>()
        val pendingInstructorLookups = mutableListOf<Pair<String, Int>>()

        Log.d(TAG, "Processing ${documents.size} timetable documents")

        documents.forEachIndexed { index, data ->
            try {
                val day = data["day"] as? String ?: ""
                val time = data["time"] as? String ?: ""
                val courseCode = data["courseCode"] as? String ?: data["subject"] as? String ?: ""
                val room = data["room"] as? String ?: ""
                val type = data["type"] as? String ?: ""
                val teacherId = data["teacherId"] as? String
                var teacherName = data["teacherName"] as? String ?: ""

                Log.d(TAG, "Processing slot: day=$day, time=$time, course=$courseCode, room=$room, type=$type")

                // If no teacher name but we have teacher ID, queue for lookup
                if (teacherName.isEmpty() && !teacherId.isNullOrEmpty()) {
                    fetchedSlots.add(StudentSlot(day, time, courseCode, room, "", type))
                    pendingInstructorLookups.add(Pair(teacherId, index))
                } else {
                    fetchedSlots.add(StudentSlot(day, time, courseCode, room, teacherName, type))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error parsing slot data", e)
            }
        }

        if (pendingInstructorLookups.isEmpty()) {
            // No instructor lookups needed
            timeSlots.clear()
            timeSlots.addAll(fetchedSlots)
            Log.d(TAG, "Added ${timeSlots.size} time slots")
            renderTable()
        } else {
            // Perform instructor lookups
            performInstructorLookups(fetchedSlots, pendingInstructorLookups)
        }
    }

    private fun performInstructorLookups(
        slots: MutableList<StudentSlot>,
        lookups: List<Pair<String, Int>>
    ) {
        val completed = BooleanArray(lookups.size) { false }

        lookups.forEachIndexed { i, (instructorId, slotIndex) ->
            db.collection("instructors")
                .document(instructorId)
                .get()
                .addOnSuccessListener { doc ->
                    if (!isAdded) return@addOnSuccessListener

                    val instructorName = doc.getString("fullName") ?: instructorId
                    if (slotIndex < slots.size) {
                        slots[slotIndex] = slots[slotIndex].copy(teacherId = instructorName)
                    }

                    completed[i] = true

                    // Check if all lookups are complete
                    if (completed.all { it }) {
                        timeSlots.clear()
                        timeSlots.addAll(slots)
                        Log.d(TAG, "Completed instructor lookups, added ${timeSlots.size} time slots")
                        renderTable()
                    }
                }
                .addOnFailureListener { e ->
                    if (!isAdded) return@addOnFailureListener

                    Log.w(TAG, "Failed to load instructor $instructorId", e)
                    completed[i] = true

                    // Continue even if some instructor lookups fail
                    if (completed.all { it }) {
                        timeSlots.clear()
                        timeSlots.addAll(slots)
                        Log.d(TAG, "Completed instructor lookups with errors, added ${timeSlots.size} time slots")
                        renderTable()
                    }
                }
        }
    }

    private fun clearTable() {
        if (isAdded) {
            tableLayout.removeAllViews()
        }
    }

    private fun showEmptyState(message: String) {
        if (isAdded) {
            emptyView.text = message
            emptyView.visibility = View.VISIBLE
            tableLayout.visibility = View.GONE
        }
    }

    private fun renderTable() {
        if (!isAdded) return

        Log.d(TAG, "Rendering table with ${timeSlots.size} time slots")

        emptyView.visibility = View.GONE
        tableLayout.visibility = View.VISIBLE

        buildTimetableTable()
    }

    private fun buildTimetableTable() {
        // Clear existing table
        tableLayout.removeAllViews()

        val scale = resources.displayMetrics.density
        val cellWidthPx = (120 * scale).toInt()
        val paddingPx = (4 * scale).toInt()
        val minHeightPx = (80 * scale).toInt()

        val todayName = getCurrentDayName()

        Log.d(TAG, "Building table for today: $todayName")

        // Create table rows for each time slot
        for (time in times) {
            val row = TableRow(requireContext()).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Add time column (first column)
            val timeCell = TextView(requireContext()).apply {
                text = time
                setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                gravity = Gravity.CENTER
                minHeight = minHeightPx
                textSize = 12f
                layoutParams = TableRow.LayoutParams(cellWidthPx, TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundResource(R.drawable.cell_border)
            }
            row.addView(timeCell)

            // Add day columns
            for (day in days) {
                val dayCell = createDayCell(day, time, cellWidthPx, paddingPx, minHeightPx, todayName)
                row.addView(dayCell)
            }

            tableLayout.addView(row)
        }

        Log.d(TAG, "Table built with ${times.size} rows and ${days.size + 1} columns")
    }

    private fun createDayCell(
        day: String,
        time: String,
        width: Int,
        padding: Int,
        height: Int,
        todayName: String
    ): TextView {
        val cell = TextView(requireContext()).apply {
            setPadding(padding, padding, padding, padding)
            gravity = Gravity.CENTER
            minHeight = height
            textSize = 10f
            maxLines = 4
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = TableRow.LayoutParams(width, TableRow.LayoutParams.MATCH_PARENT)
        }

        // Find matching time slot
        val slot = timeSlots.find { it.day == day && it.time == time }

        if (slot != null) {
            Log.d(TAG, "Found slot for $day $time: ${slot.courseCode}")

            // Set cell content
            cell.text = "${slot.type.uppercase()}\n${slot.courseCode}\n${slot.room}\n${slot.teacherId}"

            // Set background based on course type
            when (slot.type.lowercase()) {
                "cour" -> cell.setBackgroundResource(R.drawable.cour_cell_background)
                "td" -> cell.setBackgroundResource(R.drawable.td_cell_background)
                "tp" -> cell.setBackgroundResource(R.drawable.tp_cell_background)
                else -> cell.setBackgroundResource(R.drawable.cell_border)
            }

            // Highlight today's classes
            if (day.equals(todayName, true)) {
                cell.animateHighlight()
            }
        } else {
            // Empty cell
            cell.setBackgroundResource(R.drawable.cell_border)
            cell.text = ""
        }

        return cell
    }

    private fun TextView.animateHighlight() {
        ValueAnimator.ofObject(
            ArgbEvaluator(),
            Color.TRANSPARENT,
            Color.parseColor("#9AF0FF")
        ).apply {
            duration = 1000
            addUpdateListener { animator ->
                setBackgroundColor(animator.animatedValue as Int)
            }
            start()
        }
    }

    private fun getCurrentDayName(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            else -> ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timetableListener?.remove()
    }
}