package com.example.univbouira.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.models.Student

class StudentAdapter(
    private val students: List<Student>,
    private val onItemClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.studentName)
        val idTextView: TextView = itemView.findViewById(R.id.studentId)

        fun bind(student: Student, isSelected: Boolean) {
            nameTextView.text = student.name
            idTextView.text = "ID: ${student.id}"
            itemView.isSelected = isSelected
            itemView.setBackgroundColor(
                if (isSelected)
                    itemView.context.getColor(R.color.teal_200) // selected background color
                else
                    itemView.context.getColor(android.R.color.transparent)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        val isSelected = position == selectedPosition
        holder.bind(student, isSelected)

        holder.itemView.setOnClickListener {
            val adapterPos = holder.adapterPosition
            if (adapterPos == RecyclerView.NO_POSITION) return@setOnClickListener

            if (selectedPosition != adapterPos) {
                val previousPosition = selectedPosition
                selectedPosition = adapterPos
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                Toast.makeText(holder.itemView.context, "Selected: ${student.name}", Toast.LENGTH_SHORT).show()
                onItemClick(student)
            }
        }
    }

    override fun getItemCount(): Int = students.size
}
