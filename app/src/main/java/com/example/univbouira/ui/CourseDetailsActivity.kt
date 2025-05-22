package com.example.univbouira.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univbouira.adapters.MaterialAdapter
import com.example.univbouira.databinding.ActivityCourseDetailsBinding
import com.example.univbouira.models.UploadedFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CourseDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseDetailsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val files = mutableListOf<UploadedFile>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseCode = intent.getStringExtra("courseCode") ?: run {
            Toast.makeText(this, "Course code missing", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        val courseName = intent.getStringExtra("courseName") ?: ""
        val professorName = intent.getStringExtra("professorName") ?: ""

        binding.courseNameText.text = courseName
        binding.professorNameText.text = "By $professorName"

        binding.recyclerViewMaterials.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMaterials.adapter = MaterialAdapter(this, files)

        // Fetch student level
        val email = auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        db.collection("students")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { snaps ->
                val level = snaps.firstOrNull()?.getString("level") ?: "L3"
                loadFiles(level, courseCode)
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to fetch student info", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFiles(level: String, courseCode: String) {
        db.collection("levels")
            .document(level)
            .collection("courses")
            .document(courseCode)
            .collection("uploads")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { snaps ->
                files.clear()
                for (doc in snaps) {
                    files.add(doc.toObject(UploadedFile::class.java).copy(id = doc.id))
                }
                binding.progressBar.visibility = View.GONE

                if (files.isEmpty()) {
                    binding.noMaterialsText.visibility = View.VISIBLE
                } else {
                    binding.recyclerViewMaterials.visibility = View.VISIBLE
                    binding.recyclerViewMaterials.adapter?.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load materials", Toast.LENGTH_SHORT).show()
            }
    }
}
