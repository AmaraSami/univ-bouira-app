package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    lateinit var loginbtn: Button
    lateinit var log1: TextInputLayout
    lateinit var log2: TextInputLayout
    lateinit var numero_carte_etudiant_ET: TextInputEditText
    lateinit var passwordET: TextInputEditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_activity)
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()

        numero_carte_etudiant_ET.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {

                log1.hint = null
            } else {
                log1.hint = "numero de carte d’etudiant"
            }
        }
        passwordET.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {

                log2.hint = null
            } else {
                log2.hint = "mot de passe d’etudiant"
            }
        }

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToMain()
        }

        val username = "123456789"
        val password = "123456789"

        loginbtn.setOnClickListener {
            val enteredUsername = numero_carte_etudiant_ET.text.toString()
            val enteredPassword = passwordET.text.toString()

            if (enteredUsername == username && enteredPassword == password) {
                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                navigateToMain()
            } else {
                // Show error message
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }


    }


    private fun initializeViews() {
        loginbtn = findViewById(R.id.loginbtn)
        log1 = findViewById(R.id.log1)
        log2 = findViewById(R.id.log2)
        numero_carte_etudiant_ET = findViewById(R.id.input_nce)
        passwordET = findViewById(R.id.input_mdp)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}