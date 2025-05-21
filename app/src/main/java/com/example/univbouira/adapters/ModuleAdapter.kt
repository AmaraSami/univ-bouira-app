package com.example.univbouira.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.databinding.ItemModuleBinding
import com.example.univbouira.models.ModuleItem

class ModuleAdapter(
    private val onItemClick: (ModuleItem) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    private var moduleList: List<ModuleItem> = emptyList()

    fun updateModules(newModuleList: List<ModuleItem>) {
        moduleList = newModuleList
        notifyDataSetChanged()
    }

    inner class ModuleViewHolder(private val binding: ItemModuleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(module: ModuleItem) {
            binding.textModuleName.text = module.title
            binding.textModuleLevel.text = module.subtitle.ifEmpty { "Niveau inconnu" }

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
