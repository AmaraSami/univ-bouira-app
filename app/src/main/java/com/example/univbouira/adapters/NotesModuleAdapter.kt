package com.example.univbouira.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.models.ModuleWithLevel

class NotesModuleAdapter(
    private val onModuleClick: (ModuleWithLevel) -> Unit
) : RecyclerView.Adapter<NotesModuleAdapter.ModuleViewHolder>() {

    private var modules = listOf<ModuleWithLevel>()

    inner class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val codeText: TextView = itemView.findViewById(R.id.moduleCodeText)
        val titleText: TextView = itemView.findViewById(R.id.moduleTitleText)
        val levelText: TextView = itemView.findViewById(R.id.moduleLevelText)

        fun bind(module: ModuleWithLevel) {
            codeText.text = module.code
            titleText.text = module.title
            levelText.text = module.level

            itemView.setOnClickListener {
                onModuleClick(module)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notes_item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(modules[position])
    }

    override fun getItemCount(): Int = modules.size

    fun updateModules(newModules: List<ModuleWithLevel>) {
        modules = newModules
        notifyDataSetChanged()
    }
}
