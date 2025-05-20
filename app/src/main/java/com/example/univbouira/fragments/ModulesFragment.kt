package com.example.univbouira.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univbouira.adapters.ModuleAdapter
import com.example.univbouira.databinding.FragmentModulesBinding
import com.example.univbouira.models.LearningCourse
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
            Toast.makeText(requireContext(), "Clicked: ${module.name}", Toast.LENGTH_SHORT).show()
            // You can start an activity or show bottom sheet here
        }

        binding.recyclerViewModules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moduleAdapter
        }
    }

    private fun loadModules() {
        showModuleLoading(true)
        db.collection("courses")
            .whereEqualTo("semester", selectedSemester)
            .get()
            .addOnSuccessListener { result ->
                val modules = result.map { it.toObject(LearningCourse::class.java) }
                showModuleLoading(false)
                if (modules.isEmpty()) {
                    binding.recyclerViewModules.visibility = View.GONE
                    binding.emptyModuleMessage.visibility = View.VISIBLE
                } else {
                    moduleAdapter.updateModules(modules)
                    binding.recyclerViewModules.visibility = View.VISIBLE
                    binding.emptyModuleMessage.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                showModuleLoading(false)
                Toast.makeText(requireContext(), "Error loading modules", Toast.LENGTH_SHORT).show()
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
