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
            val email = "$emailPrefix@univ-bouira.dz"
            val password = binding.inputMdp.text.toString().trim()

            if (emailPrefix.isEmpty() || password.length != 12) {
                Toast.makeText(this, "Email ou mot de passe invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Show progress and disable button
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
                .addOnSuccessListener { result ->
                    if (result.size() != 1) {
                        auth.signOut()
                        Toast.makeText(this, "Profil dupliqué ou manquant", Toast.LENGTH_LONG).show()
                        resetLoginUI()
                        return@addOnSuccessListener
                    }

                    val profile = result.documents[0]
                    val sharedPref = getSharedPreferences("StudentPrefs", MODE_PRIVATE)
                    sharedPref.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("email", userEmail)
                        .apply()

                    Toast.makeText(this, "Connexion réussie 🎉", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erreur Firestore", Toast.LENGTH_SHORT).show()
                    resetLoginUI()
                }

        }.addOnFailureListener {
            Toast.makeText(this, "Échec de connexion", Toast.LENGTH_SHORT).show()
            resetLoginUI()
        }
    }

    // ✅ Reset UI state after login attempt
    private fun resetLoginUI() {
        binding.loginbtn.isEnabled = true
        binding.loginProgress.visibility = View.GONE
    }

    // ✅ Hint behavior on focus
    private fun setupHintOnFocus() {
        binding.inputNce.setOnFocusChangeListener { _, hasFocus ->
            binding.inputNce.hint = if (hasFocus) "" else "Email"
        }

        binding.inputMdp.setOnFocusChangeListener { _, hasFocus ->
            binding.inputMdp.hint = if (hasFocus) "" else "Mot de Passe"
        }

        binding.inputNce.hint = "Email"
        binding.inputMdp.hint = "Mot de Passe"
    }
}
