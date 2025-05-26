package com.example.univbouira.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.models.NotesItem
import androidx.core.graphics.toColorInt

class NotesAdapter(
    private val notesList: List<NotesItem>,
    private val onItemClick: (NotesItem) -> Unit // Add this lambda
) : RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {

    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moduleName: TextView = itemView.findViewById(R.id.module_name)
        private val moyenneText: TextView = itemView.findViewById(R.id.moyenne)

        fun bind(item: NotesItem) {
            moduleName.text = item.moduleName
            moyenneText.text = "Moyenne: %.2f".format(item.moyenne)

            moyenneText.setTextColor(
                when {
                    item.moyenne > 10 -> "#206B26".toColorInt()
                    item.moyenne > 0 -> "#880808".toColorInt()
                    else -> Color.GRAY
                }
            )

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notes_moyenne, parent, false)
        return NotesViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bind(notesList[position])
    }

    override fun getItemCount(): Int = notesList.size
}

