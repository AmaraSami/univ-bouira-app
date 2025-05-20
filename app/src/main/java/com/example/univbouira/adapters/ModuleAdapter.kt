package com.example.univbouira.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.databinding.ItemModuleBinding
import com.example.univbouira.models.LearningCourse

class ModuleAdapter(
    private val onItemClick: (LearningCourse) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    private var moduleList: List<LearningCourse> = emptyList()

    fun updateModules(newModuleList: List<LearningCourse>) {
        moduleList = newModuleList
        notifyDataSetChanged()
    }

    inner class ModuleViewHolder(private val binding: ItemModuleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(module: LearningCourse) {
            binding.textModuleName.text = module.name
            binding.textModuleLevel.text = module.level ?: "Niveau inconnu"

            binding.root.setOnClickListener {
                onItemClick(module)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val binding = ItemModuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(moduleList[position])
    }

    override fun getItemCount(): Int = moduleList.size
}
