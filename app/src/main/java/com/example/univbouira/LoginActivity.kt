package com.example.univbouira

import android.content.Intent
import android.os.Bundle
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

        binding.loginbtn.setOnClickListener {
            val email = binding.inputNce.text.toString().trim()
            val password = binding.inputMdp.text.toString().trim()

            if (!email.endsWith("@univ-bouira.dz")) {
                Toast.makeText(this, "Invalid university email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            val userEmail = auth.currentUser?.email ?: return@addOnSuccessListener

            // First check in students
            db.collection("students")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { studentResult ->
                    if (studentResult.size() == 1 && password.length == 12) {
                        // Student detected
                        val sharedPref = getSharedPreferences("StudentPrefs", MODE_PRIVATE)
                        sharedPref.edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("email", userEmail)
                            .putString("role", "student")
                            .apply()

                        Toast.makeText(this, "Logged in as Student", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Not a student? Check in instructors
                        db.collection("instructors")
                            .whereEqualTo("email", userEmail)
                            .get()
                            .addOnSuccessListener { instructorResult ->
                                if (instructorResult.size() == 1) {
                                    // Instructor detected
                                    val sharedPref = getSharedPreferences("StudentPrefs", MODE_PRIVATE)
                                    sharedPref.edit()
                                        .putBoolean("isLoggedIn", true)
                                        .putString("email", userEmail)
                                        .putString("role", "instructor")
                                        .apply()

                                    Toast.makeText(this, "Logged in as Instructor", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                } else {
                                    auth.signOut()
                                    Toast.makeText(this, "Profile not found or duplicated", Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error checking instructor", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error checking student", Toast.LENGTH_SHORT).show()
                }

        }.addOnFailureListener {
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveLoginState(role: String, email: String) {
        val sharedPref = getSharedPreferences("StudentPrefs", MODE_PRIVATE)
        sharedPref.edit()
            .putBoolean("isLoggedIn", true)
            .putString("email", email)
            .putString("role", role)
            .apply()

        Toast.makeText(this, "Login successful 🎉", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
