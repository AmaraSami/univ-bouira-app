package com.example.univbouira.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.models.ModuleItem

class AssignedCoursesAdapter(private val courses: List<ModuleItem>) :
    RecyclerView.Adapter<AssignedCoursesAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseImageView: ImageView = itemView.findViewById(R.id.courseImageView)
        val courseNameTextView: TextView = itemView.findViewById(R.id.courseNameTextView)
        val courseCodeTextView: TextView = itemView.findViewById(R.id.courseCodeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_profile, parent, false) // Make sure the file is named item_course_profile.xml
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.courseNameTextView.text = course.title
        holder.courseCodeTextView.text = course.code
        holder.courseImageView.setImageResource(R.drawable.ic_course_placeholder)
    }

    override fun getItemCount(): Int = courses.size
}
