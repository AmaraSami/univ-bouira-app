package com.example.univbouira.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.databinding.ItemCourseMaterialBinding
import com.example.univbouira.models.UploadedFile

class ManageCoursesAdapter(
    private val onDeleteClick: (UploadedFile) -> Unit
) : RecyclerView.Adapter<ManageCoursesAdapter.ViewHolder>() {

    private val uploadedFiles = mutableListOf<UploadedFile>()

    fun updateData(newFiles: List<UploadedFile>) {
        uploadedFiles.clear()
        uploadedFiles.addAll(newFiles)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemCourseMaterialBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: UploadedFile) {
            binding.textFileName.text = file.name
            binding.textModuleTitle.text = "Module: ${file.moduleTitle}"

            binding.buttonDelete.setOnClickListener {
                onDeleteClick(file)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCourseMaterialBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(uploadedFiles[position])
    }

    override fun getItemCount(): Int = uploadedFiles.size
}
