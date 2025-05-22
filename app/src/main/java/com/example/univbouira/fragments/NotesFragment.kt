package com.example.univbouira.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univbouira.R
import com.example.univbouira.adapters.NotesAdapter
import com.example.univbouira.databinding.FragmentNotesBinding
import com.example.univbouira.models.ModuleItem
import com.example.univbouira.models.NotesItems
import com.example.univbouira.ui.ModuleNotesDetailActivity
import com.google.firebase.firestore.FirebaseFirestore

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotesAdapter
    private val itemList = mutableListOf<NotesItems>()
    private val db = FirebaseFirestore.getInstance()
    private var selectedSemester = "semestre1"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupSemesterButtons()

        val sharedPref = requireContext().getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE)
        val nce = sharedPref.getString("cardId", null)

        if (nce.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Identifiant étudiant manquant", Toast.LENGTH_SHORT).show()
        } else {
            loadModulesFromFirebase(selectedSemester)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(itemList) { module ->
            val intent = Intent(requireContext(), ModuleNotesDetailActivity::class.java)
            intent.putExtra("moduleTitle", module.title)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSemesterButtons() {
        highlightSelectedSemester()

        binding.semester1Button.setOnClickListener {
            selectedSemester = "semestre1"
            highlightSelectedSemester()
            loadModulesFromFirebase(selectedSemester)
        }

        binding.semester2Button.setOnClickListener {
            selectedSemester = "semestre2"
            highlightSelectedSemester()
            loadModulesFromFirebase(selectedSemester)
        }
    }

    private fun highlightSelectedSemester() {
        val selectedColor = Color.parseColor("#007BA7")
        val defaultColor = Color.parseColor("#BDBDBD")

        binding.semester1Button.setBackgroundColor(
            if (selectedSemester == "semestre1") selectedColor else defaultColor
        )
        binding.semester2Button.setBackgroundColor(
            if (selectedSemester == "semestre2") selectedColor else defaultColor
        )}

    private fun loadModulesFromFirebase(semester: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.emptyNotesMessage.visibility = View.GONE

        db.collection("courses")
            .whereEqualTo("semester", semester)
            .get()
            .addOnSuccessListener { result ->
                itemList.clear()
                for (doc in result) {
                    val moduleName = doc.getString("name") ?: continue
                    val description = doc.getString("description") ?: "Module description"
                    itemList.add(NotesItems(moduleName, description, "--"))
                }

                adapter.notifyDataSetChanged()

                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = if (itemList.isEmpty()) View.GONE else View.VISIBLE
                binding.emptyNotesMessage.visibility = if (itemList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Erreur lors du chargement des modules", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
