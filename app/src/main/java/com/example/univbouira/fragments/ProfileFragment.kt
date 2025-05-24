package com.example.univbouira.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.univbouira.LoginActivity
import com.example.univbouira.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var nameText: TextView
    private lateinit var levelText: TextView
    private lateinit var specialtyText: TextView
    private lateinit var studentNumberText: TextView
    private lateinit var birthDateText: TextView
    private lateinit var birthPlaceText: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var logoutButton: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        nameText = view.findViewById(R.id.Sname)
        levelText = view.findViewById(R.id.Slevel)
        specialtyText = view.findViewById(R.id.specialty_id)
        studentNumberText = view.findViewById(R.id.editStudentNumber)
        birthDateText = view.findViewById(R.id.editBirthDate)
        birthPlaceText = view.findViewById(R.id.editBirthPlace)
        profileImage = view.findViewById(R.id.profile_image)
        logoutButton = view.findViewById(R.id.logoutbtn)

        val userEmail = firebaseAuth.currentUser?.email

        if (userEmail != null) {
            fetchProfileData(userEmail)
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener {
            val sharedPreferences = requireContext().getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }

        return view
    }

    private fun fetchProfileData(email: String) {
        firestore.collection("students")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    nameText.text = doc.getString("fullName")
                    levelText.text = doc.getString("level") + " | " + doc.getString("groupName")
                    specialtyText.text = doc.getString("specialty")
                    studentNumberText.text = doc.getString("studentNumber")
                    birthDateText.text = doc.getString("birthDate")
                    birthPlaceText.text = doc.getString("birthPlace")

                    val profileImageUrl = doc.getString("imageUrl")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profileImageUrl)
                            .placeholder(R.drawable.user_icn)
                            .into(profileImage)
                    }

                } else {
                    Toast.makeText(requireContext(), "No profile data found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}