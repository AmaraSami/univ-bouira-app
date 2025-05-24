package com.example.univbouira.ui

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

        moduleCode   = intent.getStringExtra("moduleCode")
        moduleTitle  = intent.getStringExtra("moduleTitle")
        groupName    = intent.getStringExtra("groupName")
        studentName  = intent.getStringExtra("studentName")
        studentId    = intent.getStringExtra("studentId")

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
            .setPositiveButton("Yes") { _, _ -> saveGrades() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveGrades() {
        val tdStr   = binding.editTextGradeTD.text.toString().trim()
        val tpStr   = binding.editTextGradeTP.text.toString().trim()
        val examStr = binding.editTextGradeExam.text.toString().trim()

        if (tdStr.isEmpty() || tpStr.isEmpty() || examStr.isEmpty()) {
            Toast.makeText(this, "Please fill all grades", Toast.LENGTH_SHORT).show()
            return
        }

        val tdValue   = tdStr.toDoubleOrNull()
        val tpValue   = tpStr.toDoubleOrNull()
        val examValue = examStr.toDoubleOrNull()

        if (tdValue == null || tpValue == null || examValue == null) {
            Toast.makeText(this, "Please enter valid numeric grades", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidGrade(tdValue) || !isValidGrade(tpValue) || !isValidGrade(examValue)) {
            Toast.makeText(this, "Grades must be between 0 and 20", Toast.LENGTH_SHORT).show()
            return
        }

        if (moduleCode.isNullOrBlank() || groupName.isNullOrBlank() || studentId.isNullOrBlank()) {
            Toast.makeText(this, "Missing required data", Toast.LENGTH_SHORT).show()
            return
        }

        // Compute the moyenne: TP & TD average weighted 40%, exam weighted 60%
        val tdTdAverage = (tdValue + tpValue) / 2.0
        val moyenne = tdTdAverage * 0.4 + examValue * 0.6

        toggleLoading(true)

        val gradeData = hashMapOf(
            "TD"          to tdValue,
            "TP"          to tpValue,
            "EXAM"        to examValue,
            "moyenne"     to moyenne,
            "studentName" to studentName,
            "studentId"   to studentId,
            "moduleCode"  to moduleCode,
            "groupName"   to groupName,
            "timestamp"   to System.currentTimeMillis()
        )

        val docRef = db.collection("grades")
            .document(moduleCode!!)
            .collection(groupName!!)
            .document(studentId!!)

        docRef.set(gradeData)
            .addOnSuccessListener {
                toggleLoading(false)
                Toast.makeText(this, "Grades & moyenne saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                toggleLoading(false)
                Toast.makeText(this, "Failed to save grades: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.progressSaving.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSaveGrades.isEnabled   = !isLoading
        binding.buttonSaveGrades.alpha       = if (isLoading) 0.5f else 1f
        binding.editTextGradeTD.isEnabled    = !isLoading
        binding.editTextGradeTP.isEnabled    = !isLoading
        binding.editTextGradeExam.isEnabled  = !isLoading
    }

    private fun isValidGrade(value: Double): Boolean = value in 0.0..20.0
}
