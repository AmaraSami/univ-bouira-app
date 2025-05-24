package com.example.univbouira.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.univbouira.LoginActivity
import com.example.univbouira.R
import com.example.univbouira.databinding.FragmentInstructorProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InstructorProfileFragment : Fragment() {

    private var _binding: FragmentInstructorProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadInstructorData()
        setupLogout()
    }

    private fun loadInstructorData() {
        val user = auth.currentUser ?: return
        firestore.collection("instructors")
            .whereEqualTo("email", user.email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val name = doc.getString("fullName") ?: "N/A"
                    val email = doc.getString("email") ?: "N/A"
                    val cardNumber = doc.getString("cardNumber") ?: "N/A"
                    val birthDate = doc.getString("birthDate") ?: "N/A"
                    val birthPlace = doc.getString("birthPlace") ?: "N/A"
                    val imageUrl = doc.getString("imageUrl")

                    binding.instructorName.text = name
                    binding.instructorEmail.text = email
                    binding.editInstructorStudentNumber.text = cardNumber
                    binding.editInstructorBirthDate.text = birthDate
                    binding.editInstructorBirthPlace.text = birthPlace

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.user_icn)
                            .into(binding.profileImage)
                    }


                } else {
                    Toast.makeText(requireContext(), "Instructor not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupLogout() {
        binding.logoutbtn.setOnClickListener {
            auth.signOut()
            requireActivity().getSharedPreferences("StudentPrefs", 0).edit().clear().apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}