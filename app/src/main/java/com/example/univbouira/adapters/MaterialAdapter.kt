package com.example.univbouira.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.models.UploadedFile

class MaterialAdapter(
    private val context: Context,
    private val materials: List<UploadedFile>
) : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.materialTitle)
        val type: TextView = v.findViewById(R.id.materialType)
        val viewButton: Button = v.findViewById(R.id.viewButton)
        val downloadButton: Button = v.findViewById(R.id.downloadButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_material, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = materials[position]
        holder.title.text = file.name
        holder.type.text = file.moduleTitle

        // View via external PDF app/browser
        holder.viewButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(file.url)
                setDataAndType(data, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        }

        // Download via system DownloadManager
        holder.downloadButton.setOnClickListener {
            val dmIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(file.url)
            }
            context.startActivity(dmIntent)
        }
    }

    override fun getItemCount(): Int = materials.size
}
