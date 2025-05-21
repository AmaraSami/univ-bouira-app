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
import java.util.*

class ManageCoursesFragment : Fragment() {

    private var _binding: FragmentCourseMaterialsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ManageCoursesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var moduleCode: String = ""
    private var moduleTitle: String = ""

    companion object {
        fun newInstance(moduleCode: String, moduleTitle: String): ManageCoursesFragment {
            val fragment = ManageCoursesFragment()
            val args = Bundle()
            args.putString("moduleCode", moduleCode)
            args.putString("moduleTitle", moduleTitle)
            fragment.arguments = args
            return fragment
        }
    }

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
        val fileName = UUID.randomUUID().toString()
        val fileRef = storageRef.child("course_materials/$fileName")

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
                val file = UploadedFile(
                    id = null,  // assuming id is nullable and assigned by Firestore
                    name = getFileNameFromUri(fileUri) ?: "file",
                    url = uri.toString(),
                    timestamp = System.currentTimeMillis(),
                    moduleCode = moduleCode,
                    moduleTitle = moduleTitle
                )

                db.collection("course_materials")
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
        // Optional: Implement method to get file name from uri if needed
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    private fun fetchUploadedFiles() {
        binding.courseLoading.visibility = View.VISIBLE
        db.collection("course_materials")
            .whereEqualTo("moduleCode", moduleCode)
            .get()
            .addOnSuccessListener { result ->
                val files = result.map { doc ->
                    val file = doc.toObject(UploadedFile::class.java)
                    file.id = doc.id // Assign Firestore doc ID for deletion reference
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

        // Delete from Firebase Storage
        val storageRef = storage.reference.child("course_materials/${file.name}")
        storageRef.delete()
            .addOnSuccessListener {
                // Then delete from Firestore
                db.collection("course_materials").document(file.id!!)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
