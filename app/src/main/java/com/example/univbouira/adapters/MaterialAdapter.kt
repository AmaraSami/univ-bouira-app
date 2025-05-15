package com.example.univbouira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.models.CourseMaterial

class MaterialAdapter(
    private val context: android.content.Context,
    private val materials: List<CourseMaterial>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder>() {

    interface OnItemClickListener {
        fun onDownloadClick(material: CourseMaterial)
        fun onViewClick(material: CourseMaterial)
    }

    inner class MaterialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.materialTitle)
        val typeText: TextView = itemView.findViewById(R.id.materialType)
        val downloadButton: Button = itemView.findViewById(R.id.downloadButton)
        val viewButton: Button = itemView.findViewById(R.id.viewButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_material, parent, false)
        return MaterialViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val material = materials[position]
        holder.titleText.text = material.title
        holder.typeText.text = material.type.capitalize()

        holder.downloadButton.setOnClickListener {
            listener.onDownloadClick(material)
        }

        holder.viewButton.setOnClickListener {
            listener.onViewClick(material)
        }
    }

    override fun getItemCount(): Int = materials.size
}
