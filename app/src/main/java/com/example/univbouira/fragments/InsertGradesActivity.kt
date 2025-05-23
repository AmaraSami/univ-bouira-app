package com.example.univbouira.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.univbouira.databinding.ActivityInsertGradesBinding
import com.google.firebase.firestore.FirebaseFirestore

class InsertGradesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsertGradesBinding
    private val db = FirebaseFirestore.getInstance()

    private var moduleCode: String? = null
    private var moduleTitle: String? = null
    private var groupName: String? = null
    private var studentName: String? = null
    private var studentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsertGradesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moduleCode = intent.getStringExtra("moduleCode")
        moduleTitle = intent.getStringExtra("moduleTitle")
        groupName = intent.getStringExtra("groupName")
        studentName = intent.getStringExtra("studentName")
        studentId = intent.getStringExtra("studentId")

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.textInsertGradesTitle.text = "Grades for $studentName"
    }

    private fun setupListeners() {
        binding.buttonSaveGrades.setOnClickListener {
            showConfirmDialog()
        }
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Save")
            .setMessage("Are you sure you want to save these grades?")
            .setPositiveButton("Yes") { _, _ ->
                saveGrades()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveGrades() {
        val gradeTD = binding.editTextGradeTD.text.toString().trim()
        val gradeTP = binding.editTextGradeTP.text.toString().trim()
        val gradeExam = binding.editTextGradeExam.text.toString().trim()

        if (gradeTD.isEmpty() || gradeTP.isEmpty() || gradeExam.isEmpty()) {
            Toast.makeText(this, "Please fill all grades", Toast.LENGTH_SHORT).show()
            return
        }

        val tdValue = gradeTD.toDoubleOrNull()
        val tpValue = gradeTP.toDoubleOrNull()
        val examValue = gradeExam.toDoubleOrNull()

        if (tdValue == null || tpValue == null || examValue == null) {
            Toast.makeText(this, "Please enter valid numeric grades", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidGrade(tdValue) || !isValidGrade(tpValue) || !isValidGrade(examValue)) {
            Toast.makeText(this, "Grades must be between 0 and 20", Toast.LENGTH_SHORT).show()
            return
        }

        if (moduleCode == null || groupName == null || studentId == null) {
            Toast.makeText(this, "Missing required data", Toast.LENGTH_SHORT).show()
            return
        }

        toggleLoading(true)

        val gradeData = hashMapOf(
            "TD" to tdValue,
            "TP" to tpValue,
            "EXAM" to examValue,
            "studentName" to studentName,
            "studentId" to studentId,
            "moduleCode" to moduleCode,
            "groupName" to groupName,
            "timestamp" to System.currentTimeMillis()
        )

        val docRef = db.collection("grades")
            .document(moduleCode!!)
            .collection(groupName!!)
            .document(studentId!!)

        docRef.set(gradeData)
            .addOnSuccessListener {
                toggleLoading(false)
                Toast.makeText(this, "Grades saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                toggleLoading(false)
                Toast.makeText(this, "Failed to save grades: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.progressSaving.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSaveGrades.isEnabled = !isLoading
        binding.buttonSaveGrades.alpha = if (isLoading) 0.5f else 1f
        // Optional: disable input fields while loading
        binding.editTextGradeTD.isEnabled = !isLoading
        binding.editTextGradeTP.isEnabled = !isLoading
        binding.editTextGradeExam.isEnabled = !isLoading
    }

    private fun isValidGrade(value: Double): Boolean {
        return value in 0.0..20.0
    }
}
