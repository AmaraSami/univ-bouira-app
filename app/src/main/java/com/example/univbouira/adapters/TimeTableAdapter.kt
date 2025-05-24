package com.example.univbouira.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.models.StudentSlot
import java.util.Calendar

class TimeTableAdapter(
    private val context: Context,
    private val timeSlots: List<StudentSlot>
) : RecyclerView.Adapter<TimeTableAdapter.TimeSlotViewHolder>() {

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectText: TextView = itemView.findViewById(R.id.subject_text)
        val roomText: TextView    = itemView.findViewById(R.id.room_text)
        val teacherText: TextView = itemView.findViewById(R.id.teacher_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val slot = timeSlots[position]

        // Bind fields matching StudentSlot model
        holder.subjectText.text = slot.courseCode
        holder.roomText.text    = slot.room
        holder.teacherText.text = slot.teacherId

        // Color-code by slot type background
        when (slot.type.lowercase()) {
            "cour" -> holder.itemView.setBackgroundResource(R.drawable.cour_cell_background)
            "td"   -> holder.itemView.setBackgroundResource(R.drawable.td_cell_background)
            "tp"   -> holder.itemView.setBackgroundResource(R.drawable.tp_cell_background)
            else    -> holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        // Highlight today's slots by slot.day
        val todayName = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY  -> "Saturday"
            Calendar.SUNDAY    -> "Sunday"
            Calendar.MONDAY    -> "Monday"
            Calendar.TUESDAY   -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY  -> "Thursday"
            else               -> ""
        }
        if (slot.day.equals(todayName, ignoreCase = true)) {
            holder.itemView.setBackgroundColor(
                context.getColor(R.color.highlight_day)
            )
        }
    }

    override fun getItemCount(): Int = timeSlots.size
}
