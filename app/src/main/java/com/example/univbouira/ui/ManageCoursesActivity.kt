package com.example.univbouira.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.adapters.ManageCoursesAdapter
import com.example.univbouira.models.UploadedFile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ManageCoursesActivity : AppCompatActivity() {

    private lateinit var adapter: ManageCoursesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var moduleCode: String = ""
    private var moduleTitle: String = ""

    private lateinit var textModuleTitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var courseLoading: ProgressBar
    private lateinit var emptyCourseMessage: TextView
    private lateinit var buttonUploadCourse: ImageButton

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uploadFileToFirebase(it) }
                ?: Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_courses)

        moduleCode = intent.getStringExtra("courseCode") ?: ""
        moduleTitle = intent.getStringExtra("courseTitle") ?: ""

        textModuleTitle = findViewById(R.id.textModuleTitle)
        recyclerView = findViewById(R.id.recyclerViewCourses)
        courseLoading = findViewById(R.id.courseLoading)
        emptyCourseMessage = findViewById(R.id.emptyCourseMessage)
        buttonUploadCourse = findViewById(R.id.buttonUploadCourse)

        textModuleTitle.text = moduleTitle

        setupRecyclerView()
        fetchUploadedFiles()

        buttonUploadCourse.setOnClickListener { openFilePicker() }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            ))
        }
        filePickerLauncher.launch(intent)
    }

    private fun uploadFileToFirebase(fileUri: Uri) {
        courseLoading.visibility = View.VISIBLE
        buttonUploadCourse.isEnabled = false

        val uuidFileName = UUID.randomUUID().toString()
        val storagePath = "modules/$moduleCode/$uuidFileName"
        val fileRef = storage.reference.child(storagePath)

        fileRef.putFile(fileUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                fileRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                val fileName = getFileNameFromUri(fileUri) ?: "file"

                val file = UploadedFile(
                    id = null,
                    name = fileName,
                    url = downloadUri.toString(),
                    storagePath = storagePath,
                    timestamp = System.currentTimeMillis(),
                    moduleCode = moduleCode,
                    moduleTitle = moduleTitle
                )

                // Debug logs before writing to Firestore
                Log.d("UPLOAD_DEBUG", "Preparing to add Firestore document:")
                Log.d("UPLOAD_DEBUG", "→ moduleCode: $moduleCode")
                Log.d("UPLOAD_DEBUG", "→ storagePath: $storagePath")
                Log.d("UPLOAD_DEBUG", "→ download URL: $downloadUri")
                Log.d("UPLOAD_DEBUG", "→ file name: $fileName")
                Log.d("UPLOAD_DEBUG", "→ writing to Firestore: levels/L3/courses/$moduleCode/uploads")

                val uploadsRef = db.collection("levels").document("L3")
                    .collection("courses").document(moduleCode)
                    .collection("uploads")

                uploadsRef.add(file)
                    .addOnSuccessListener { docRef ->
                        Log.d("UPLOAD_DEBUG", "✅ Firestore .add() succeeded: ${docRef.path}")
                        Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
                        fetchUploadedFiles()
                    }
                    .addOnFailureListener { e ->
                        Log.e("UPLOAD_DEBUG", "❌ Firestore .add() FAILED: ${e.message}", e)
                        Toast.makeText(this,
                            "Failed to save file info: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        courseLoading.visibility = View.GONE
                        buttonUploadCourse.isEnabled = true
                    }
                    .addOnCompleteListener {
                        courseLoading.visibility = View.GONE
                        buttonUploadCourse.isEnabled = true
                    }

            }
            .addOnFailureListener { e ->
                Log.e("UPLOAD_DEBUG", "Storage upload failed: ${e.message}", e)
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                courseLoading.visibility = View.GONE
                buttonUploadCourse.isEnabled = true
            }
    }


    private fun getFileNameFromUri(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex("_display_name")
                if (index != -1) return cursor.getString(index)
            }
        }
        return uri.lastPathSegment
    }

    private fun fetchUploadedFiles() {
        courseLoading.visibility = View.VISIBLE
        emptyCourseMessage.visibility = View.GONE

        db.collection("levels").document("L3")
            .collection("courses").document(moduleCode)
            .collection("uploads")
            .get()
            .addOnSuccessListener { result ->
                val files = result.map { doc ->
                    doc.toObject(UploadedFile::class.java).apply { id = doc.id }
                }
                adapter.updateData(files)
                emptyCourseMessage.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load files", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                courseLoading.visibility = View.GONE
            }
    }


    private fun setupRecyclerView() {
        adapter = ManageCoursesAdapter { uploadedFile -> deleteUploadedFile(uploadedFile) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun deleteUploadedFile(file: UploadedFile) {
        if (file.id == null || file.storagePath.isEmpty()) {
            Toast.makeText(this, "Invalid file reference", Toast.LENGTH_SHORT).show()
            return
        }

        courseLoading.visibility = View.VISIBLE
        buttonUploadCourse.isEnabled = false

        val storageRef = storage.reference.child(file.storagePath)
        Log.d("DELETE_DEBUG", "Attempting Storage.delete() on path: ${storageRef.path}")

        storageRef.delete()
            .addOnSuccessListener {
                Log.d("DELETE_DEBUG", "✅ Storage.delete() succeeded")
                deleteFirestoreEntry(file)
            }
            .addOnFailureListener { e ->
                if (e is com.google.firebase.storage.StorageException
                    && e.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND
                ) {
                    // Treat “already gone” as success
                    Log.w("DELETE_DEBUG", "⚠️ Object already deleted; proceeding to Firestore cleanup")
                    deleteFirestoreEntry(file)
                } else {
                    Log.e("DELETE_DEBUG", "❌ Storage.delete() FAILED: ${e.message}", e)
                    Toast.makeText(this, "Failed to delete storage file: ${e.message}", Toast.LENGTH_SHORT).show()
                    courseLoading.visibility = View.GONE
                    buttonUploadCourse.isEnabled = true
                }
            }
    }

    private fun deleteFirestoreEntry(file: UploadedFile) {
        val docRef = db.collection("levels")
            .document("L3")
            .collection("courses").document(moduleCode)
            .collection("uploads").document(file.id!!)

        Log.d("DELETE_DEBUG", "Deleting Firestore entry at: ${docRef.path}")
        docRef.delete()
            .addOnSuccessListener {
                Log.d("DELETE_DEBUG", "✅ Firestore.delete() succeeded")
                Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show()
                fetchUploadedFiles()
            }
            .addOnFailureListener { e ->
                Log.e("DELETE_DEBUG", "❌ Firestore.delete() FAILED: ${e.message}", e)
                Toast.makeText(this, "Failed to delete Firestore entry: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                courseLoading.visibility = View.GONE
                buttonUploadCourse.isEnabled = true
            }
    }

}
