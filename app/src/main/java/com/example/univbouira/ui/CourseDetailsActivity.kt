package com.example.univbouira.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univbouira.adapter.MaterialAdapter
import com.example.univbouira.databinding.ActivityCourseDetailsBinding
import com.example.univbouira.models.CourseMaterial
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class CourseDetailsActivity : AppCompatActivity(), MaterialAdapter.OnItemClickListener {

    private lateinit var binding: ActivityCourseDetailsBinding
    private lateinit var adapter: MaterialAdapter
    private val materialList = mutableListOf<CourseMaterial>()

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isConnected()) {
                Toast.makeText(this@CourseDetailsActivity, "No internet connection", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseName = intent.getStringExtra("courseName") ?: ""
        val professorName = intent.getStringExtra("professorName") ?: ""

        binding.courseNameText.text = courseName
        binding.professorNameText.text = "By $professorName"

        adapter = MaterialAdapter(this, materialList, this)
        binding.recyclerViewMaterials.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMaterials.adapter = adapter

        loadCourseMaterials(courseName)
    }

    private fun loadCourseMaterials(courseName: String) {
        db.collection("materials")
            .whereEqualTo("courseName", courseName)
            .get()
            .addOnSuccessListener { result ->
                materialList.clear()
                for (doc in result) {
                    val material = doc.toObject(CourseMaterial::class.java)
                    materialList.add(material)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load materials", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDownloadClick(material: CourseMaterial) {
        if (!isConnected()) {
            Toast.makeText(this, "No internet. Please connect to download.", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = storage.getReferenceFromUrl(material.fileUrl)
        val localFile = File(getExternalFilesDir(null), material.fileName)

        ref.getFile(localFile).addOnSuccessListener {
            Toast.makeText(this, "Downloaded to ${localFile.path}", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewClick(material: CourseMaterial) {
        val localFile = File(getExternalFilesDir(null), material.fileName)
        if (!localFile.exists()) {
            Toast.makeText(this, "File not downloaded. Please download it first.", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.fromFile(localFile)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, getMimeType(material.fileName))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".pdf") -> "application/pdf"
            fileName.endsWith(".doc") || fileName.endsWith(".docx") -> "application/msword"
            fileName.endsWith(".ppt") || fileName.endsWith(".pptx") -> "application/vnd.ms-powerpoint"
            fileName.endsWith(".xls") || fileName.endsWith(".xlsx") -> "application/vnd.ms-excel"
            fileName.endsWith(".txt") -> "text/plain"
            else -> "*/*"
        }
    }

    private fun isConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkReceiver)
    }
}
