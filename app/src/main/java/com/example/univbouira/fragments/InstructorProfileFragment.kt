package com.example.univbouira.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.univbouira.LoginActivity
import com.example.univbouira.adapters.ModuleAdapter
import com.example.univbouira.databinding.FragmentInstructorProfileBinding
import com.example.univbouira.models.ModuleItem
import com.example.univbouira.ui.ManageCoursesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InstructorProfileFragment : Fragment() {

    private lateinit var binding: FragmentInstructorProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var moduleAdapter: ModuleAdapter
    private var instructorUid: String? = null
    private var assignedCourses: List<String> = emptyList()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInstructorProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadInstructorProfile()

        binding.logoutbtn.setOnClickListener {
            val sharedPreferences = requireContext().getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        moduleAdapter = ModuleAdapter { selectedModule ->
            val intent = Intent(requireContext(), ManageCoursesActivity::class.java).apply {
                putExtra("courseCode", selectedModule.code)
                putExtra("courseTitle", selectedModule.title)
            }
            startActivity(intent)
        }

        binding.assignedCoursesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moduleAdapter
        }
    }

    private fun loadInstructorProfile() {
        instructorUid = auth.currentUser?.uid
        if (instructorUid.isNullOrEmpty()) return

        firestore.collection("instructors").document(instructorUid!!)
            .get()
            .addOnSuccessListener { document ->
                val fullName = document.getString("fullName") ?: "N/A"
                val email = document.getString("email") ?: "N/A"
                assignedCourses = document.get("assignedCourses") as? List<String> ?: emptyList()

                binding.instructorName.text = fullName
                binding.instructorEmail.text = email

                val imageUrl = document.getString("imageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(imageUrl).into(binding.profileImage)
                } else {
                    binding.profileImage.setImageResource(com.example.univbouira.R.drawable.user_icn)
                }

                loadAssignedCourses()
            }
    }

    private fun loadAssignedCourses() {
        val levelsToCheck = listOf("L1", "L2", "L3", "M1", "M2")
        val moduleList = mutableListOf<ModuleItem>()

        for (level in levelsToCheck) {
            firestore.collection("levels")
                .document(level)
                .collection("courses")
                .get()
                .addOnSuccessListener { documents ->
                    for (doc in documents) {
                        val code = doc.getString("code") ?: continue
                        val title = doc.getString("title") ?: continue

                        if (assignedCourses.contains(code)) {
                            moduleList.add(ModuleItem(code = code, title = title))
                        }
                    }
                    moduleAdapter.updateModules(moduleList)
                }
        }
    }

}
