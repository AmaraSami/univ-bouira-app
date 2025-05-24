package com.example.univbouira.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.univbouira.databinding.ModuleNotesDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class ModuleNotesDetailActivity : AppCompatActivity() {

    private lateinit var binding: ModuleNotesDetailBinding
    private lateinit var moduleCode: String
    private lateinit var moduleTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ModuleNotesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moduleCode = intent.getStringExtra("moduleCode") ?: ""
        moduleTitle = intent.getStringExtra("moduleTitle") ?: moduleCode
        binding.moduleTitle.text = "Module: $moduleTitle"

        val sharedPref = getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE)
        val studentNumber = sharedPref.getString("cardId", null)
        val groupName = sharedPref.getString("groupName", null)

        if (studentNumber.isNullOrEmpty() || groupName.isNullOrEmpty()) {
            Toast.makeText(this, "Données étudiant manquantes", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchNotesFromFirebase(studentNumber, groupName)

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchNotesFromFirebase(studentNumber: String, groupName: String) {
        val db = FirebaseFirestore.getInstance()
        val gradeRef = db.collection("grades")
            .document(moduleCode)
            .collection(groupName)
            .document(studentNumber)

        gradeRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val td = document.getDouble("td")
                    val tp = document.getDouble("tp")
                    val exam = document.getDouble("exam")

                    val moyenne = calculateMoyenne(tp, td, exam)

                    updateNoteUI(tp, td, exam)

                    if (moyenne != null) {
                        // 🟢 Save moyenne to Firestore
                        gradeRef.update("moyenne", moyenne)
                            .addOnSuccessListener {
                                // Optional: Log or toast success
                                // Toast.makeText(this, "Moyenne enregistrée", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Erreur lors de l'enregistrement de la moyenne", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Aucune note pour ce module", Toast.LENGTH_SHORT).show()
                    updateNoteUI(null, null, null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur de chargement des notes", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateNoteUI(tp: Double?, td: Double?, exam: Double?) {
        binding.tpNote.text = tp?.let { "Note TP: $it" } ?: "Note TP: --"
        binding.tdNote.text = td?.let { "Note TD: $it" } ?: "Note TD: --"
        binding.examNote.text = exam?.let { "Note Exam: $it" } ?: "Note Exam: --"

        val moyenne = calculateMoyenne(tp, td, exam)

        if (moyenne != null) {
            binding.moyenne.text = "Moyenne: %.2f".format(moyenne)
            binding.status.text = if (moyenne >= 10.0) "Status: Admis" else "Status: Ajourné"
        } else {
            binding.moyenne.text = "Moyenne: --"
            binding.status.text = "Status: En attente"
        }

        if (tp == null && td == null && exam == null) {
            Toast.makeText(this, "Aucune note disponible pour ce module.", Toast.LENGTH_LONG).show()
        }
    }

    private fun calculateMoyenne(tp: Double?, td: Double?, exam: Double?): Double? {
        return when {
            tp != null && td != null && exam != null ->
                ((tp + td) / 2.0) * 0.4 + exam * 0.6
            tp != null && exam != null ->
                tp * 0.4 + exam * 0.6
            td != null && exam != null ->
                td * 0.4 + exam * 0.6
            exam != null -> exam
            tp != null -> tp
            td != null -> td
            else -> null
        }
    }
}
