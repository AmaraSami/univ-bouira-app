package com.example.univbouira.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.univbouira.databinding.ModuleNotesDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class ModuleNotesDetailActivity : AppCompatActivity() {

    private lateinit var binding: ModuleNotesDetailBinding
    private lateinit var moduleName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ModuleNotesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moduleName = intent.getStringExtra("moduleTitle") ?: ""
        binding.moduleTitle.text = "Module: $moduleName"

        val sharedPref = getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE)
        val studentNumber = sharedPref.getString("cardId", null)

        if (studentNumber != null) {
            fetchNotesFromFirebase(studentNumber)
        } else {
            Toast.makeText(this, "Identifiant étudiant manquant", Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchNotesFromFirebase(studentNumber: String) {
        FirebaseFirestore.getInstance()
            .collection("notes")
            .document(studentNumber)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val notes = document.get(moduleName) as? Map<*, *>
                    val td = notes?.get("td")?.toString()?.toDoubleOrNull()
                    val tp = notes?.get("tp")?.toString()?.toDoubleOrNull()
                    val exam = notes?.get("exam")?.toString()?.toDoubleOrNull()

                    updateNoteUI(tp, td, exam)
                } else {
                    Toast.makeText(this, "Aucune note pour ce module", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur de chargement des notes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateNoteUI(tp: Double?, td: Double?, exam: Double?) {
        binding.tpNote.text = if (tp != null) "Note TP: $tp" else "Note TP: --"
        binding.tdNote.text = if (td != null) "Note TD: $td" else "Note TD: --"
        binding.examNote.text = if (exam != null) "Note Exam: $exam" else "Note Exam: --"

        val validNotes = listOfNotNull(tp, td, exam)

        if (validNotes.isNotEmpty()) {
            val average = validNotes.average()
            binding.moyenne.text = "Moyenne: %.2f".format(average)
            binding.status.text = if (average >= 10.0) "Status: Admis" else "Status: Ajourné"
        } else {
            binding.moyenne.text = "Moyenne: --"
            binding.status.text = "Status: En attente"
        }

        if (tp == null && td == null && exam == null) {
            Toast.makeText(this, "Aucune note disponible pour ce module.", Toast.LENGTH_LONG).show()
        }
    }
}
