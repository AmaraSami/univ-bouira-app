package com.example.univbouira.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univbouira.R
import com.example.univbouira.adapters.ModuleAdapter
import com.example.univbouira.databinding.FragmentModulesBinding
import com.example.univbouira.models.ModuleItem
import com.example.univbouira.ui.ManageCoursesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ModulesFragment : Fragment() {

    private var _binding: FragmentModulesBinding? = null
    private val binding get() = _binding!!

    private lateinit var moduleAdapter: ModuleAdapter
    private val db = FirebaseFirestore.getInstance()
    private var selectedSemester = "Semestre 1"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModulesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSemesterButtons()
        loadModules()
    }

    private fun setupSemesterButtons() {
        updateButtonColors()

        binding.semester1Button.setOnClickListener {
            selectedSemester = "Semestre 1"
            updateButtonColors()
            loadModules()
        }

        binding.semester2Button.setOnClickListener {
            selectedSemester = "Semestre 2"
            updateButtonColors()
            loadModules()
        }
    }

    private fun updateButtonColors() {
        if (_binding == null) return
        val selectedColor = Color.parseColor("#007BA7")
        val defaultColor = Color.parseColor("#BDBDBD")

        binding.semester1Button.setBackgroundColor(
            if (selectedSemester == "Semestre 1") selectedColor else defaultColor
        )
        binding.semester2Button.setBackgroundColor(
            if (selectedSemester == "Semestre 2") selectedColor else defaultColor
        )
    }

    private fun setupRecyclerView() {
        moduleAdapter = ModuleAdapter { module ->
            val intent = Intent(requireContext(), ManageCoursesActivity::class.java).apply {
                putExtra("courseCode", module.code)
                putExtra("courseTitle", module.title)
            }
            startActivity(intent)
        }

        binding.recyclerViewModules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moduleAdapter
        }
    }

    private fun loadModules() {
        showModuleLoading(true)

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val selectedSemesterNumber = if (selectedSemester == "Semestre 1") 1 else 2
        val allLevels = listOf("L1", "L2", "L3" , "M1" , "M2")

        db.collection("instructors")
            .whereEqualTo("email", currentUserEmail)
            .get()
            .addOnSuccessListener { result ->
                if (_binding == null) return@addOnSuccessListener

                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "Instructor not found", Toast.LENGTH_SHORT).show()
                    showModuleLoading(false)
                    return@addOnSuccessListener
                }

                val assignedCourses = result.documents[0]
                    .get("assignedCourses") as? List<String> ?: emptyList()

                val matchedModules = mutableListOf<ModuleItem>()
                var levelsProcessed = 0

                allLevels.forEach { level ->
                    db.collection("levels")
                        .document(level)
                        .collection("courses")
                        .whereEqualTo("semester", selectedSemesterNumber)
                        .get()
                        .addOnSuccessListener { courses ->
                            if (_binding == null) return@addOnSuccessListener

                            for (doc in courses) {
                                val code = doc.id
                                val title = doc.getString("title") ?: continue
                                val semester = (doc.getLong("semester") ?: selectedSemesterNumber.toLong()).toInt()

                                if (assignedCourses.contains(code)) {
                                    matchedModules.add(ModuleItem(code, title, semester))
                                }
                            }

                            levelsProcessed++
                            if (levelsProcessed == allLevels.size) {
                                if (_binding == null) return@addOnSuccessListener
                                showModuleLoading(false)
                                if (matchedModules.isEmpty()) {
                                    binding.recyclerViewModules.visibility = View.GONE
                                    binding.emptyModuleMessage.visibility = View.VISIBLE
                                } else {
                                    moduleAdapter.updateModules(matchedModules)
                                    binding.recyclerViewModules.visibility = View.VISIBLE
                                    binding.emptyModuleMessage.visibility = View.GONE
                                }
                            }
                        }
                        .addOnFailureListener {
                            if (_binding == null) return@addOnFailureListener
                            levelsProcessed++
                            if (levelsProcessed == allLevels.size) showModuleLoading(false)
                            Toast.makeText(requireContext(), "Failed loading $level modules", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener
                showModuleLoading(false)
                Toast.makeText(requireContext(), "Error fetching instructor data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showModuleLoading(loading: Boolean) {
        if (_binding == null) return
        binding.moduleLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}