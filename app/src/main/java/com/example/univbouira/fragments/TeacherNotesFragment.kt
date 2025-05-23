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
import com.example.univbouira.activities.GroupListActivity
import com.example.univbouira.adapters.ModuleAdapter
import com.example.univbouira.databinding.FragmentModulesBinding
import com.example.univbouira.models.ModuleItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TeacherNotesFragment : Fragment() {

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
            val intent = Intent(requireContext(), GroupListActivity::class.java).apply {
                putExtra("moduleCode", module.code)
                putExtra("moduleTitle", module.title)
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
        val allLevels = listOf("L1", "L2", "L3")

        val instructorRef = db.collection("instructors").whereEqualTo("email", currentUserEmail)

        instructorRef.get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                Toast.makeText(requireContext(), "Instructor not found", Toast.LENGTH_SHORT).show()
                showModuleLoading(false)
                return@addOnSuccessListener
            }

            val instructorDoc = result.documents[0]
            val assignedCourses = instructorDoc.get("assignedCourses") as? List<String> ?: emptyList()

            val matchedModules = mutableListOf<ModuleItem>()
            var levelsProcessed = 0

            allLevels.forEach { level ->
                db.collection("levels")
                    .document(level)
                    .collection("courses")
                    .whereEqualTo("semester", selectedSemesterNumber)
                    .get()
                    .addOnSuccessListener { courses ->
                        for (doc in courses) {
                            val code = doc.id
                            val title = doc.getString("title") ?: continue
                            val semester = (doc.get("semester") as? Long)?.toInt() ?: selectedSemesterNumber

                            if (assignedCourses.contains(code)) {
                                val module = ModuleItem(
                                    code = code,
                                    title = title,
                                    semester = semester
                                )
                                matchedModules.add(module)
                            }
                        }

                        levelsProcessed++
                        if (levelsProcessed == allLevels.size) {
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
                        showModuleLoading(false)
                        Toast.makeText(requireContext(), "Failed loading $level modules", Toast.LENGTH_SHORT).show()
                    }
            }

        }.addOnFailureListener {
            showModuleLoading(false)
            Toast.makeText(requireContext(), "Error fetching instructor data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showModuleLoading(loading: Boolean) {
        binding.moduleLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
