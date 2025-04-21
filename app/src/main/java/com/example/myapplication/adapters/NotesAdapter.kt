package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ModuleItem
import com.example.myapplication.R

class NotesAdapter(private val itemList: List<ModuleItem>) : RecyclerView.Adapter<NotesAdapter.ModuleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val item = itemList[position]
        holder.titleText.text = item.title
        holder.subtitleText.text = item.subtitle
        holder.noteText.text = item.note
    }

    override fun getItemCount(): Int = itemList.size


    class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.module_title_text)
        val subtitleText: TextView = itemView.findViewById(R.id.module_subtitle_text)
        val noteText: TextView = itemView.findViewById(R.id.module_note_text)
    }


}