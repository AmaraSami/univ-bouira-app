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

            if (!email.endsWith("@univ-bouira.dz") || password.length != 12) {
                Toast.makeText(this, "Email ou mot de passe invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            val userEmail = auth.currentUser?.email ?: return@addOnSuccessListener

            // ✅ Check if profile exists and is unique
            db.collection("students")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { result ->
                    if (result.size() != 1) {
                        auth.signOut()
                        Toast.makeText(this, "Profil dupliqué ou manquant", Toast.LENGTH_LONG).show()
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
                }

        }.addOnFailureListener {
            Toast.makeText(this, "Échec de connexion", Toast.LENGTH_SHORT).show()
        }
    }
}
