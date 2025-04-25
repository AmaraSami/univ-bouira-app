package com.example.myapplication.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.dataClass.Course
import com.example.myapplication.dataClass.TimeSlot
import java.util.Calendar


// TimeTableAdapter.kt
class TimeTableAdapter(private val context: Context, private val timeSlots: List<TimeSlot>) :
    RecyclerView.Adapter<TimeTableAdapter.TimeSlotViewHolder>() {

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectText: TextView = itemView.findViewById(R.id.subject_text)
        val roomText: TextView = itemView.findViewById(R.id.room_text)
        val teacherText: TextView = itemView.findViewById(R.id.teacher_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val slot = timeSlots[position]
        holder.subjectText.text = slot.subject
        holder.roomText.text = slot.room
        holder.teacherText.text = slot.teacher

        // Highlight if it's the current day
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ..., 7=Saturday
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Sat") // no Friday
        val currentDayName = dayNames.getOrNull((currentDay - 1).coerceAtMost(5)) // avoid out of bounds

        if (slot.day.equals(currentDayName, ignoreCase = true)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.highlight_day))
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }


    override fun getItemCount(): Int = timeSlots.size

}