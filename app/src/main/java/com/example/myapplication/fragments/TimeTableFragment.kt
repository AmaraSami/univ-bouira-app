package com.example.myapplication.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.dataClass.TimeSlot

class TimeTableFragment : Fragment(R.layout.fragment_time_table) {

    private lateinit var tableLayout: TableLayout
    private lateinit var timeSlots: List<TimeSlot>

    // Use full day names for comparing with data, but abbreviated names will be used in UI
    private val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")
    private val times = listOf("08:00-09:30", "09:30-11:00", "11:00-12:30", "12:30-14:00", "14:00-15:30")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableLayout = view.findViewById(R.id.tableLayout)

        // Sample data
        timeSlots = listOf(
            TimeSlot("Saturday", "08:00-09:30", "Maths", "R101", "Dr. Bensalah", "Cour"),
            TimeSlot("Saturday", "09:30-11:00", "Physics", "R102", "Dr. Khelifa", "TD"),
            TimeSlot("Saturday", "11:00-12:30", "Programming", "Lab1", "Ms. Zahra", "TP"),

            TimeSlot("Sunday", "08:00-09:30", "Networking", "R201", "Dr. Lounis", "Cour"),
            TimeSlot("Sunday", "09:30-11:00", "Electronics", "R202", "Dr. Hachemi", "TD"),
            TimeSlot("Sunday", "11:00-12:30", "Maths", "R203", "Dr. Bensalah", "TP"),

            TimeSlot("Monday", "08:00-09:30", "Algorithms", "R301", "Mr. Sami", "Cour"),
            TimeSlot("Monday", "09:30-11:00", "Programming", "Lab2", "Ms. Zahra", "TD"),
            TimeSlot("Monday", "11:00-12:30", "Discrete Math", "R302", "Dr. Khadidja", "TP"),

            TimeSlot("Tuesday", "08:00-09:30", "Databases", "R401", "Dr. Karim", "Cour"),
            TimeSlot("Tuesday", "09:30-11:00", "Physics", "R402", "Dr. Khelifa", "TD"),
            TimeSlot("Tuesday", "14:00-15:30", "Electronics", "Lab3", "Dr. Hachemi", "TP"),

            TimeSlot("Wednesday", "08:00-09:30", "Operating Systems", "R501", "Dr. Sofiane", "Cour"),
            TimeSlot("Wednesday", "09:30-11:00", "Maths", "R502", "Dr. Bensalah", "TD"),
            TimeSlot("Wednesday", "14:00-15:30", "Programming", "Lab2", "Ms. Zahra", "TP"),

            TimeSlot("Thursday", "08:00-09:30", "Databases", "R601", "Dr. Karim", "Cour"),
            TimeSlot("Thursday", "09:30-11:00", "Networking", "R602", "Dr. Lounis", "TD"),
            TimeSlot("Thursday", "11:00-12:30", "Electronics", "Lab3", "Dr. Hachemi", "TP")
        )

        // Set up click listeners for semester buttons
        view.findViewById<View>(R.id.semester5_button).setOnClickListener {
            // Load semester 5 data
            // For now, just use the current data
            buildTable()
        }

        view.findViewById<View>(R.id.semester6_button).setOnClickListener {
            // Load semester 6 data
            // For now, just use the current data
            buildTable()
        }

        // Clear any existing rows and build the table
        tableLayout.removeAllViews()
        buildTable()
    }

    private fun buildTable() {
        // Use default display metrics to convert dp to px
        val scale = resources.displayMetrics.density
        val cellWidthPx = (120 * scale).toInt() // Increased from 100dp to 120dp for more space
        val paddingPx = (4 * scale).toInt() // Reduced from 8dp to 4dp for compact cells
        val minHeightPx = (80 * scale).toInt() // Increased from 70dp to 80dp for more content

        // Clear existing table rows
        tableLayout.removeAllViews()

        for (time in times) {
            val tableRow = TableRow(requireContext())
            tableRow.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            // Disable dividers between cells
            tableRow.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE)

            // Time column with fixed width
            val timeTextView = TextView(requireContext())
            timeTextView.text = time
            timeTextView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            timeTextView.gravity = Gravity.CENTER
            timeTextView.minHeight = minHeightPx
            timeTextView.textSize = 12f // Smaller text for better fit

            // No margins in layout params
            val timeParams = TableRow.LayoutParams(cellWidthPx, TableRow.LayoutParams.MATCH_PARENT)
            timeParams.setMargins(0, 0, 0, 0)
            timeTextView.layoutParams = timeParams

            // Apply border background
            timeTextView.setBackgroundResource(R.drawable.cell_border)

            tableRow.addView(timeTextView)

            // Loop over each day
            for (day in days) {
                val cellTextView = TextView(requireContext())
                cellTextView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                cellTextView.gravity = Gravity.CENTER
                cellTextView.minHeight = minHeightPx
                cellTextView.textSize = 12f
                cellTextView.maxLines = 4 // Limit lines
                cellTextView.ellipsize = TextUtils.TruncateAt.END // Show ellipsis if needed

                // No margins in layout params
                val cellParams = TableRow.LayoutParams(cellWidthPx, TableRow.LayoutParams.MATCH_PARENT)
                cellParams.setMargins(0, 0, 0, 0)
                cellTextView.layoutParams = cellParams

                // Always set a border background (for empty cells too)
                cellTextView.setBackgroundResource(R.drawable.cell_border)

                val slot = timeSlots.find { it.day == day && it.time == time }
                if (slot != null) {
                    // Format text for better fit
                    cellTextView.text = "${slot.type}\n${slot.subject}\n${slot.room}\n${slot.teacher}"

                    // Color based on class type
                    when (slot.type.lowercase()) {
                        "cour" -> cellTextView.setBackgroundResource(R.drawable.cour_cell_background)
                        "td" -> cellTextView.setBackgroundResource(R.drawable.td_cell_background)
                        "tp" -> cellTextView.setBackgroundResource(R.drawable.tp_cell_background)
                    }
                } else {
                    // Empty cell still needs a background with borders
                    cellTextView.setBackgroundResource(R.drawable.cell_border)
                    cellTextView.text = "" // Ensure empty text
                }

                tableRow.addView(cellTextView)
            }

            tableLayout.addView(tableRow)
        }
    }
}