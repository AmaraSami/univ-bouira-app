package com.example.univbouira.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univbouira.adapters.CourseAdapter
import com.example.univbouira.adapters.InstructorAdapter
import com.example.univbouira.databinding.BottomsheetInstructorCoursesBinding
import com.example.univbouira.databinding.FragmentCoursesBinding
import com.example.univbouira.models.Instructor
import com.example.univbouira.models.LearningCourse
import com.example.univbouira.ui.CourseDetailsActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CoursesFragment : Fragment() {

    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private var selectedSemester = "Semestre 1"
    private var lastInstructorClickTime = 0L
    private var lastCourseClickTime = 0L

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var instructorAdapter: InstructorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        updateButtonColors()

        loadStudentName()
        loadInstructors()
        loadCourses()

        binding.semester1Button.setOnClickListener {
            selectedSemester = "Semestre 1"
            updateButtonColors()
            loadInstructors()
            loadCourses()
        }

        binding.semester2Button.setOnClickListener {
            selectedSemester = "Semestre 2"
            updateButtonColors()
            loadInstructors()
            loadCourses()
        }
    }

    private fun updateButtonColors() {
        val selectedColor = Color.parseColor("#007BA7")
        val defaultColor = Color.parseColor("#BDBDBD")

        binding.semester1Button.setBackgroundColor(
            if (selectedSemester == "Semestre 1") selectedColor else defaultColor
        )
        binding.semester2Button.setBackgroundColor(
            if (selectedSemester == "Semestre 2") selectedColor else defaultColor
        )
    }

    private fun setupRecyclerViews() {
        courseAdapter = CourseAdapter { course ->
            val now = System.currentTimeMillis()
            if (now - lastCourseClickTime < 1000) return@CourseAdapter
            lastCourseClickTime = now

            val intent = Intent(requireContext(), CourseDetailsActivity::class.java).apply {
                putExtra("courseName", course.name)
                putExtra("professorName", course.professor)
            }
            startActivity(intent)
        }

        instructorAdapter = InstructorAdapter(requireContext(), emptyList()) { instructor ->
            val now = System.currentTimeMillis()
            if (now - lastInstructorClickTime < 1000) return@InstructorAdapter
            lastInstructorClickTime = now
            fetchInstructorCourses(instructor)
        }

        binding.recyclerViewCourses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewCourses.adapter = courseAdapter

        binding.recyclerViewInstructors.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewInstructors.adapter = instructorAdapter
    }

    private fun loadStudentName() {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return

        // Try to get from students collection first
        db.collection("students")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { studentResult ->
                if (!studentResult.isEmpty) {
                    val name = studentResult.documents[0].getString("fullName") ?: "Student"
                    binding.welcomeText.text = "Hi, $name 👋"
                } else {
                    // Not a student? Try instructors
                    db.collection("instructors")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { instructorResult ->
                            if (!instructorResult.isEmpty) {
                                val name = instructorResult.documents[0].getString("name") ?: "Instructor"
                                binding.welcomeText.text = "Hi, $name 👋"
                            } else {
                                binding.welcomeText.text = "Hi 👋"
                            }
                        }
                        .addOnFailureListener {
                            binding.welcomeText.text = "Hi 👋"
                        }
                }
            }
            .addOnFailureListener {
                binding.welcomeText.text = "Hi 👋"
            }
    }


    private fun loadInstructors() {
        showInstructorLoading(true)
        db.collection("instructors")
            .whereEqualTo("semester", selectedSemester)
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { it.toObject(Instructor::class.java) }
                showInstructorLoading(false)
                if (list.isEmpty()) {
                    binding.recyclerViewInstructors.visibility = View.GONE
                    binding.emptyInstructorMessage.visibility = View.VISIBLE
                } else {
                    instructorAdapter.updateList(list)
                    binding.recyclerViewInstructors.visibility = View.VISIBLE
                    binding.emptyInstructorMessage.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                showInstructorLoading(false)
                Toast.makeText(requireContext(), "Error loading instructors", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showInstructorLoading(loading: Boolean) {
        binding.instructorLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun loadCourses() {
        showCourseLoading(true)
        db.collection("courses")
            .whereEqualTo("semester", selectedSemester)
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { it.toObject(LearningCourse::class.java) }
                showCourseLoading(false)
                if (list.isEmpty()) {
                    binding.recyclerViewCourses.visibility = View.GONE
                    binding.emptyCourseMessage.visibility = View.VISIBLE
                } else {
                    courseAdapter.updateCourses(list)
                    binding.recyclerViewCourses.visibility = View.VISIBLE
                    binding.emptyCourseMessage.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                showCourseLoading(false)
                Toast.makeText(requireContext(), "Error loading courses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCourseLoading(loading: Boolean) {
        binding.courseLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun fetchInstructorCourses(instructor: Instructor) {
        db.collection("courses")
            .whereEqualTo("professor", instructor.name)
            .whereEqualTo("semester", selectedSemester)
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { it.toObject(LearningCourse::class.java) }
                showInstructorCoursesBottomSheet(instructor.name, list)
            }
    }

    private fun showInstructorCoursesBottomSheet(name: String, list: List<LearningCourse>) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomsheetInstructorCoursesBinding.inflate(layoutInflater)
        sheetBinding.instructorTitle.text = "Courses by $name"

        val adapter = CourseAdapter {}
        adapter.updateCourses(list)
        sheetBinding.recyclerViewCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        dialog.setContentView(sheetBinding.root)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
