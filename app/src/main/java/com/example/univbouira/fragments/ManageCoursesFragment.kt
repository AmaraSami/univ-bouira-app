package com.example.univbouira.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univbouira.adapters.ManageCoursesAdapter
import com.example.univbouira.databinding.FragmentCourseMaterialsBinding
import com.example.univbouira.models.UploadedFile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.net.URLDecoder
import java.util.*

class ManageCoursesFragment : Fragment() {

    private var _binding: FragmentCourseMaterialsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ManageCoursesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var moduleCode: String = ""
    private var moduleTitle: String = ""


    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val fileUri = result.data?.data
            if (fileUri != null) {
                uploadFileToFirebase(fileUri)
            } else {
                Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCourseMaterialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moduleCode = arguments?.getString("moduleCode") ?: ""
        moduleTitle = arguments?.getString("moduleTitle") ?: ""

        binding.textModuleTitle.text = moduleTitle

        setupRecyclerView()
        fetchUploadedFiles()

        binding.buttonUploadCourse.setOnClickListener {
            openFilePicker()
        }
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
        val storageRef = storage.reference
        val uuidFileName = UUID.randomUUID().toString()
        // Store files under course_materials/L3/{moduleCode}/UUIDfilename
        val fileRef = storageRef.child("course_materials/L3/$moduleCode/$uuidFileName")

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Uploading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        fileRef.putFile(fileUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                fileRef.downloadUrl
            }
            .addOnSuccessListener { uri ->
                val originalName = getFileNameFromUri(fileUri) ?: "file"

                val file = UploadedFile(
                    id = null,  // Firestore will assign this
                    name = originalName,
                    url = uri.toString(),
                    timestamp = System.currentTimeMillis(),
                    moduleCode = moduleCode,
                    moduleTitle = moduleTitle
                )

                db.collection("levels")
                    .document("L3")
                    .collection("courses")
                    .document(moduleCode)
                    .collection("uploads")
                    .add(file)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_SHORT).show()
                        fetchUploadedFiles()
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), "Failed to save file info", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex("_display_name")
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        if (name == null) {
            name = uri.lastPathSegment
        }
        return name
    }

    private fun fetchUploadedFiles() {
        binding.courseLoading.visibility = View.VISIBLE

        db.collection("levels")
            .document("L3")
            .collection("courses")
            .document(moduleCode)
            .collection("uploads")
            .get()
            .addOnSuccessListener { result ->
                val files = result.map { doc ->
                    val file = doc.toObject(UploadedFile::class.java)
                    file.id = doc.id  // assign Firestore doc ID
                    file
                }
                adapter.updateData(files)
                binding.courseLoading.visibility = View.GONE
                binding.emptyCourseMessage.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                binding.courseLoading.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load files", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupRecyclerView() {
        adapter = ManageCoursesAdapter { uploadedFile ->
            deleteUploadedFile(uploadedFile)
        }
        binding.recyclerViewCourses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewCourses.adapter = adapter
    }

    private fun deleteUploadedFile(file: UploadedFile) {
        if (file.id == null) {
            Toast.makeText(requireContext(), "Invalid file ID", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Deleting file...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        try {
            val encodedPath = file.url.substringAfter("/o/").substringBefore("?alt=")
            val storagePath = URLDecoder.decode(encodedPath, "UTF-8")

            val storageRef = storage.reference.child(storagePath)

            storageRef.delete()
                .addOnSuccessListener {
                    db.collection("levels")
                        .document("L3")
                        .collection("courses")
                        .document(moduleCode)
                        .collection("uploads")
                        .document(file.id!!)
                        .delete()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(requireContext(), "File deleted successfully", Toast.LENGTH_SHORT).show()
                            fetchUploadedFiles()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(requireContext(), "Failed to delete file record: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Failed to delete file from storage: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(requireContext(), "Error parsing file URL: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
