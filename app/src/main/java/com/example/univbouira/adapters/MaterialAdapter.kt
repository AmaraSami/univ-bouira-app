package com.example.univbouira.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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

        // View with appropriate app (internal for PDF/TXT, external for others)
        holder.viewButton.setOnClickListener {
            try {
                val fileExtension = file.name.substringAfterLast('.', "").lowercase()

                if (fileExtension == "pdf" || fileExtension == "txt") {
                    // Open PDF and TXT files inside the app (old behavior)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(file.url)
                        setDataAndType(data, if (fileExtension == "pdf") "application/pdf" else "text/plain")
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(intent)
                } else {
                    // Open other file types with external apps
                    val mimeType = getMimeType(file.name)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(file.url)
                        setDataAndType(data, mimeType)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "No app found to open this file type", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Download via system browser/downloader
        holder.downloadButton.setOnClickListener {
            try {
                val dmIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(file.url)
                }
                context.startActivity(dmIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Error downloading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = materials.size

    private fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt" -> "text/plain"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            else -> "application/octet-stream" // Generic binary file
        }
    }
}