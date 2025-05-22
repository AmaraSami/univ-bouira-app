package com.example.univbouira.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.databinding.ItemCourseBinding
import com.example.univbouira.models.ModuleItem

class CourseAdapter(
    private val onItemClick: (ModuleItem) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private var courseList: List<ModuleItem> = listOf()

    fun updateModuleList(newCourseList: List<ModuleItem>) {
        courseList = newCourseList
        notifyDataSetChanged()
    }

    inner class CourseViewHolder(private val binding: ItemCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(course: ModuleItem) {
            binding.courseNameTextView.text = course.title
            binding.professorNameTextView.visibility = View.GONE  // no professor data in ModuleItem

            binding.root.setOnClickListener {
                onItemClick(course)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courseList[position])
    }

    override fun getItemCount(): Int = courseList.size
}
