package com.example.univbouira.fragments

import android.app.Activity
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
import com.example.univbouira.adapters.CourseMaterialAdapter
import com.example.univbouira.databinding.FragmentCourseMaterialsBinding
import com.example.univbouira.models.UploadedFile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CourseMaterialsFragment : Fragment() {

    private var _binding: FragmentCourseMaterialsBinding? = null
    private val binding get() = _binding!!

    private lateinit var courseMaterialAdapter: CourseMaterialAdapter
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var moduleName: String = "Unknown Module" // You should set this from fragment arguments or intent

    private val uploadedFiles = mutableListOf<UploadedFile>()

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadFileToFirebase(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseMaterialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupUploadButton()
        loadUploadedFiles()
    }

    private fun setupRecyclerView() {
        courseMaterialAdapter = CourseMaterialAdapter { file ->
            deleteFile(file)
        }

        binding.recyclerViewCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = courseMaterialAdapter
        }
    }

    private fun setupUploadButton() {
        binding.buttonUploadCourse.setOnClickListener {
            // Allow user to pick any document type (pdf, pptx, doc, etc)
            pickFileLauncher.launch("*/*")
        }
    }

    private fun loadUploadedFiles() {
        showLoading(true)
        db.collection("course_materials")
            .whereEqualTo("moduleName", moduleName)
            .get()
            .addOnSuccessListener { result ->
                uploadedFiles.clear()
                for (doc in result) {
                    val file = doc.toObject(UploadedFile::class.java).copy(id = doc.id)
                    uploadedFiles.add(file)
                }
                courseMaterialAdapter.updateData(uploadedFiles)
                toggleEmptyView(uploadedFiles.isEmpty())
                showLoading(false)
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Failed to load course materials", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadFileToFirebase(uri: Uri) {
        val fileName = UUID.randomUUID().toString()
        val fileRef = storage.reference.child("course_materials/$fileName")

        showLoading(true)
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveFileInfoToFirestore(fileName, downloadUrl.toString(), uri)
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveFileInfoToFirestore(fileName: String, fileUrl: String, fileUri: Uri) {
        val uploadedFile = UploadedFile(
            id = "", // Firestore doc ID assigned later
            fileName = getFileNameFromUri(fileUri),
            fileUrl = fileUrl,
            moduleName = moduleName
        )

        db.collection("course_materials")
            .add(uploadedFile)
            .addOnSuccessListener { documentReference ->
                val fileWithId = uploadedFile.copy(id = documentReference.id)
                uploadedFiles.add(fileWithId)
                courseMaterialAdapter.updateData(uploadedFiles)
                toggleEmptyView(false)
                showLoading(false)
                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Failed to save file info", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteFile(file: UploadedFile) {
        showLoading(true)
        // Delete from Firebase Storage
        storage.reference.child("course_materials/${file.fileName}").delete()
            .addOnSuccessListener {
                // Delete from Firestore
                db.collection("course_materials").document(file.id)
                    .delete()
                    .addOnSuccessListener {
                        uploadedFiles.remove(file)
                        courseMaterialAdapter.updateData(uploadedFiles)
                        toggleEmptyView(uploadedFiles.isEmpty())
                        showLoading(false)
                        Toast.makeText(requireContext(), "File deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        showLoading(false)
                        Toast.makeText(requireContext(), "Failed to delete file info", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Failed to delete file from storage", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleEmptyView(isEmpty: Boolean) {
        binding.recyclerViewCourses.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyCourseMessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.coursesLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonUploadCourse.isEnabled = !isLoading
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var result = "unknown_file"
        context?.contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                result = cursor.getString(nameIndex)
            }
        }
        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
