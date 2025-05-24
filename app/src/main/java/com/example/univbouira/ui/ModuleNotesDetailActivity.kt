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

        moduleCode  = intent.getStringExtra("moduleCode") ?: ""
        moduleTitle = intent.getStringExtra("moduleTitle") ?: moduleCode
        binding.moduleTitle.text = "Module: $moduleTitle"

        val sharedPref     = getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE)
        val studentNumber  = sharedPref.getString("cardId", null)
        val groupName      = sharedPref.getString("groupName", null)

        if (studentNumber.isNullOrEmpty() || groupName.isNullOrEmpty()) {
            Toast.makeText(this, "Missing student data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchNotesFromFirebase(studentNumber, groupName)
        binding.backButton.setOnClickListener { finish() }
    }

    private fun fetchNotesFromFirebase(studentNumber: String, groupName: String) {
        val db = FirebaseFirestore.getInstance()
        val gradeRef = db.collection("grades")
            .document(moduleCode)
            .collection(groupName)
            .document(studentNumber)

        gradeRef.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "No grades found for this module", Toast.LENGTH_SHORT).show()
                    updateNoteUI(null, null, null, null)
                    return@addOnSuccessListener
                }

                // pull all four at once
                val tp      = doc.getDouble("TP")
                val td      = doc.getDouble("TD")
                val exam    = doc.getDouble("EXAM")
                val moyenne = doc.getDouble("moyenne")

                updateNoteUI(tp, td, exam, moyenne)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading grades", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateNoteUI(
        tp: Double?,
        td: Double?,
        exam: Double?,
        moyenne: Double?
    ) {
        binding.tpNote.text   = tp?.let { "Note TP: $it" }       ?: "Note TP: --"
        binding.tdNote.text   = td?.let { "Note TD: $it" }       ?: "Note TD: --"
        binding.examNote.text = exam?.let { "Note Exam: $it" }   ?: "Note Exam: --"
        binding.moyenne.text  = moyenne?.let { "Moyenne: %.2f".format(it) }
            ?: "Moyenne: --"
        binding.status.text   = when {
            moyenne == null       -> "Status: En attente"
            moyenne >= 10.0        -> "Status: Admis"
            else                   -> "Status: Ajourné"
        }

        if (tp==null && td==null && exam==null && moyenne==null) {
            Toast.makeText(this, "No grades available for this module.", Toast.LENGTH_LONG).show()
        }
    }
}

