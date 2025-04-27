package com.example.myapplication.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.dataClass.TimeSlot
import java.util.Calendar

class TimeTableFragment : Fragment(R.layout.fragment_time_table) {

    private lateinit var tableLayout: TableLayout
    private lateinit var timeSlots: MutableList<TimeSlot>

    private lateinit var semester1Button: Button
    private lateinit var semester2Button: Button

    private var selectedSemester = 1

    private val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")
    private val times = listOf("08:00-09:30", "09:30-11:00", "11:00-12:30", "12:30-14:00", "14:00-15:30")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableLayout = view.findViewById(R.id.tableLayout)
        semester1Button = view.findViewById(R.id.semester1Button)
        semester2Button = view.findViewById(R.id.semester2Button)

        timeSlots = mutableListOf()

        semester1Button.setOnClickListener {
            if (selectedSemester != 1) {
                selectedSemester = 1
                loadTimeTableData()
                updateButtonColors()
            }
        }

        semester2Button.setOnClickListener {
            if (selectedSemester != 2) {
                selectedSemester = 2
                loadTimeTableData()
                updateButtonColors()
            }
        }

        updateButtonColors()
        loadTimeTableData()
    }

    private fun loadTimeTableData() {
        timeSlots.clear()

        if (selectedSemester == 1) {
            // Dummy data for Semester 1
            timeSlots.addAll(
                listOf(
                    TimeSlot("Saturday", "08:00-09:30", "Maths", "R101", "Dr. Bensalah", "Cour"),
                    TimeSlot("Saturday", "09:30-11:00", "Physics", "R102", "Dr. Khelifa", "TD"),
                    TimeSlot("Sunday", "08:00-09:30", "Networking", "R201", "Dr. Lounis", "Cour")
                )
            )
        } else if (selectedSemester == 2) {
            // Dummy data for Semester 2
            timeSlots.addAll(
                listOf(
                    TimeSlot("Saturday", "08:00-09:30", "Advanced Maths", "R301", "Dr. Nouri", "Cour"),
                    TimeSlot("Sunday", "08:00-09:30", "Cloud Computing", "R401", "Dr. Karim", "TD"),
                    TimeSlot("Sunday", "09:30-11:00", "AI Lab", "Lab1", "Ms. Zahra", "TP"),
                    TimeSlot("Thursday", "12:30-14:00", "AI Lab", "Lab1", "Ms. Zahra", "TP")
                )
            )
        }

        buildTable()
    }

    private fun updateButtonColors() {
        val selectedColor = Color.parseColor("#007BA7") // Blue
        val defaultColor = Color.parseColor("#BDBDBD") // Gray

        semester1Button.setBackgroundColor(if (selectedSemester == 1) selectedColor else defaultColor)
        semester2Button.setBackgroundColor(if (selectedSemester == 2) selectedColor else defaultColor)
    }

    private fun buildTable() {
        val scale = resources.displayMetrics.density
        val cellWidthPx = (120 * scale).toInt()
        val paddingPx = (4 * scale).toInt()
        val minHeightPx = (80 * scale).toInt()

        tableLayout.removeAllViews()

        val calendar = Calendar.getInstance()
        val todayIndex = calendar.get(Calendar.DAY_OF_WEEK)
        val todayName = when (todayIndex) {
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            else -> ""
        }

        for (time in times) {
            val tableRow = TableRow(requireContext())
            tableRow.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            tableRow.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE)

            val timeTextView = TextView(requireContext())
            timeTextView.text = time
            timeTextView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            timeTextView.gravity = Gravity.CENTER
            timeTextView.minHeight = minHeightPx
            timeTextView.textSize = 12f
            timeTextView.layoutParams = TableRow.LayoutParams(cellWidthPx, TableRow.LayoutParams.MATCH_PARENT)
            timeTextView.setBackgroundResource(R.drawable.cell_border)

            tableRow.addView(timeTextView)

            for (day in days) {
                val cellTextView = TextView(requireContext())
                cellTextView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                cellTextView.gravity = Gravity.CENTER
                cellTextView.minHeight = minHeightPx
                cellTextView.textSize = 12f
                cellTextView.maxLines = 4
                cellTextView.ellipsize = TextUtils.TruncateAt.END
                cellTextView.layoutParams = TableRow.LayoutParams(cellWidthPx, TableRow.LayoutParams.MATCH_PARENT)

                val slot = timeSlots.find { it.day == day && it.time == time }
                if (slot != null) {
                    cellTextView.text = "${slot.type}\n${slot.subject}\n${slot.room}\n${slot.teacher}"

                    when (slot.type.lowercase()) {
                        "cour" -> cellTextView.setBackgroundResource(R.drawable.cour_cell_background)
                        "td" -> cellTextView.setBackgroundResource(R.drawable.td_cell_background)
                        "tp" -> cellTextView.setBackgroundResource(R.drawable.tp_cell_background)
                    }

                    if (day.equals(todayName, ignoreCase = true)) {
                        val colorFrom = Color.TRANSPARENT
                        val colorTo = Color.parseColor("#9AF0FF")

                        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                        colorAnimation.duration = 1000

                        colorAnimation.addUpdateListener { animator ->
                            cellTextView.setBackgroundColor(animator.animatedValue as Int)
                        }

                        colorAnimation.start()
                    }
                } else {
                    cellTextView.setBackgroundResource(R.drawable.cell_border)
                    cellTextView.text = ""
                }

                tableRow.addView(cellTextView)
            }

            tableLayout.addView(tableRow)
        }
    }
}
