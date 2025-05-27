package com.example.univbouira

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.univbouira.databinding.LoginActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("StudentPrefs", MODE_PRIVATE)
        if (sharedPref.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupHintOnFocus()

        binding.loginbtn.setOnClickListener {
            val emailPrefix = binding.inputNce.text.toString().trim()
            val password = binding.inputMdp.text.toString().trim()
            val email = "$emailPrefix@univ-bouira.dz"

            if (emailPrefix.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.loginbtn.isEnabled = false
            binding.loginProgress.visibility = View.VISIBLE

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            val currentUser = auth.currentUser
            val userEmail = currentUser?.email ?: return@addOnSuccessListener
            val uid = currentUser.uid

            // Check if this is a student
            db.collection("students")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { studentResult ->
                    if (studentResult.size() == 1 && password.length == 12) {
                        val studentDoc = studentResult.documents[0]
                        val studentNumber = studentDoc.id
                        var groupName = studentDoc.getString("groupName") ?: ""
                        val level = studentDoc.getString("level") ?: "L3"

                        // Convert old format to new format if necessary
                        if (groupName.isNotBlank() && !groupName.contains("_GROUPE_")) {
                            // Convert formats like "G1", "GROUPE 01", "Group 1" to "L3_GROUPE_01"
                            val groupNumber = groupName.replace(Regex("[^\\d]"), "").ifEmpty { "1" }
                            groupName = "${level}_GROUPE_${groupNumber.padStart(2, '0')}"

                            // Update the database with the new format
                            studentDoc.reference.update("groupName", groupName)
                                .addOnSuccessListener {
                                    android.util.Log.d("LoginActivity", "Updated group name to: $groupName")
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.w("LoginActivity", "Failed to update group name", e)
                                }
                        }

                        if (groupName.isBlank()) {
                            auth.signOut()
                            Toast.makeText(this, "No group defined for this student", Toast.LENGTH_LONG).show()
                            resetLoginUI()
                            return@addOnSuccessListener
                        }

                        val sharedPref = getSharedPreferences("StudentPrefs", MODE_PRIVATE)
                        sharedPref.edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("email", userEmail)
                            .putString("role", "student")
                            .putString("cardId", studentNumber)
                            .putString("groupName", groupName) // Store in format like "L3_GROUPE_01"
                            .putString("level", level)
                            .apply()

                        Toast.makeText(this, "Student login successful 🎓", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()

                    } else {
                        // Instructor fallback
                        db.collection("instructors")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { instructorDoc ->
                                if (instructorDoc.exists()) {
                                    saveLoginState("instructor", userEmail)
                                } else {
                                    auth.signOut()
                                    Toast.makeText(this, "Instructor profile not found for UID", Toast.LENGTH_LONG).show()
                                    resetLoginUI()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error checking instructor profile", Toast.LENGTH_SHORT).show()
                                resetLoginUI()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while checking student profile", Toast.LENGTH_SHORT).show()
                    resetLoginUI()
                }

        }.addOnFailureListener {
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            resetLoginUI()
        }
    }

    private fun saveLoginState(role: String, email: String) {
        val sharedPref = getSharedPreferences("StudentPrefs", MODE_PRIVATE)
        sharedPref.edit()
            .putBoolean("isLoggedIn", true)
            .putString("email", email)
            .putString("role", role)
            .apply()

        val message = if (role == "student") "Student login successful 🎓" else "Instructor login successful 👨‍🏫"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun resetLoginUI() {
        binding.loginbtn.isEnabled = true
        binding.loginProgress.visibility = View.GONE
    }

    private fun setupHintOnFocus() {
        binding.inputNce.setOnFocusChangeListener { _, hasFocus ->
            binding.inputNce.hint = if (hasFocus) "" else "Email"
        }
        binding.inputMdp.setOnFocusChangeListener { _, hasFocus ->
            binding.inputMdp.hint = if (hasFocus) "" else "Password"
        }

        binding.inputNce.hint = "Email"
        binding.inputMdp.hint = "Password"
    }
}