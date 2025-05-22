package com.example.univbouira.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.databinding.ItemInstructorBinding
import com.example.univbouira.models.Instructor
import com.bumptech.glide.Glide

class InstructorAdapter(
    private val context: Context,
    private var instructorList: List<Instructor>,
    private val onItemClick: (Instructor) -> Unit
) : RecyclerView.Adapter<InstructorAdapter.InstructorViewHolder>() {

    fun updateList(newList: List<Instructor>) {
        instructorList = newList
        notifyDataSetChanged()
    }

    inner class InstructorViewHolder(val binding: ItemInstructorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructorViewHolder {
        val binding = ItemInstructorBinding.inflate(LayoutInflater.from(context), parent, false)
        return InstructorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InstructorViewHolder, position: Int) {
        val instructor = instructorList[position]
        holder.binding.instructorName.text = instructor.fullName

        Glide.with(context)
            .load(if (instructor.imageUrl.isNullOrEmpty()) R.drawable.user_icn else instructor.imageUrl)
            .placeholder(R.drawable.user_icn)
            .into(holder.binding.instructorImage)

        holder.itemView.setOnClickListener { onItemClick(instructor) }
    }

    override fun getItemCount() = instructorList.size
}
