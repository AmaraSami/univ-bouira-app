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
            val userEmail = auth.currentUser?.email ?: return@addOnSuccessListener

            db.collection("students")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { studentResult ->
                    if (studentResult.size() == 1 && password.length == 12) {
                        saveLoginState("student", userEmail)
                    } else {
                        db.collection("instructors")
                            .whereEqualTo("email", userEmail)
                            .get()
                            .addOnSuccessListener { instructorResult ->
                                if (instructorResult.size() == 1) {
                                    saveLoginState("instructor", userEmail)
                                } else {
                                    auth.signOut()
                                    Toast.makeText(this, "Profile not found or duplicated", Toast.LENGTH_LONG).show()
                                    resetLoginUI()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error while checking instructor profile", Toast.LENGTH_SHORT).show()
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
