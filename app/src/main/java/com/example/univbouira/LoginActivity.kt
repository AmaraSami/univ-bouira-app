package com.example.univbouira

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.univbouira.databinding.LoginActivityBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding
    private val client = OkHttpClient()
    private val executor = Executors.newSingleThreadExecutor()
    private val API_BASE = "https://progres.mesrs.dz"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginbtn.setOnClickListener {
            val studentNumber = binding.inputNce.text.toString().trim()
            val password = binding.inputMdp.text.toString().trim()

            if (studentNumber.length != 12 || password.length != 8) {
                Toast.makeText(this, "Vérifiez vos identifiants", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginWithApi(studentNumber, password)
        }
    }

    private fun loginWithApi(username: String, password: String) {
        executor.execute {
            try {
                val JSON = "application/json; charset=utf-8".toMediaType()
                val json = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }

                val body = RequestBody.create(JSON, json.toString())
                val request = Request.Builder()
                    .url("$API_BASE/api/authentication/v1/")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this, "Login échoué. Vérifiez vos données", Toast.LENGTH_SHORT).show()
                    }
                    return@execute
                }

                val responseBody = response.body?.string()
                val loginData = JSONObject(responseBody ?: "")

                val token = loginData.optString("token", null)
                val uuid = loginData.optString("uuid", null)

                if (token == null || uuid == null) {
                    runOnUiThread {
                        Toast.makeText(this, "Échec de l'authentification", Toast.LENGTH_SHORT).show()
                    }
                    return@execute
                }

                runOnUiThread {
                    Toast.makeText(this, "Bienvenue 👋", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Erreur réseau ou serveur", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }
}
