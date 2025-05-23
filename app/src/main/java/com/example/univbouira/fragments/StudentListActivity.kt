package com.example.univbouira.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univbouira.adapters.StudentAdapter
import com.example.univbouira.databinding.ActivityStudentListBinding
import com.example.univbouira.models.Student
import com.google.firebase.firestore.FirebaseFirestore

class StudentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentListBinding
    private lateinit var adapter: StudentAdapter
    private val db = FirebaseFirestore.getInstance()

    private var moduleCode: String? = null
    private var moduleTitle: String? = null
    private var groupName: String? = null

    private val students = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moduleCode = intent.getStringExtra("moduleCode")
        moduleTitle = intent.getStringExtra("moduleTitle")
        groupName = intent.getStringExtra("groupName")

        binding.textGroupTitle.text = "Students of $groupName"

        setupRecyclerView()
        loadStudents()
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(students) { selectedStudent ->
            val intent = Intent(this, InsertGradesActivity::class.java).apply {
                putExtra("moduleCode", moduleCode)
                putExtra("moduleTitle", moduleTitle)
                putExtra("groupName", groupName)
                putExtra("studentName", selectedStudent.name)
                putExtra("studentId", selectedStudent.id)  // pass id now
            }
            startActivity(intent)
        }
        binding.studentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.studentRecyclerView.adapter = adapter
    }

    private fun loadStudents() {
        if (groupName == null) {
            Toast.makeText(this, "Group name missing", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        db.collection("groups")
            .document(groupName!!)
            .collection("students")
            .get()
            .addOnSuccessListener { result ->
                students.clear()
                for (doc in result) {
                    val student = doc.toObject(Student::class.java)
                    students.add(student)
                }
                adapter.notifyDataSetChanged()

                showLoading(false)

                if (students.isEmpty()) {
                    binding.studentRecyclerView.visibility = View.GONE
                    binding.emptyStudentMessage.visibility = View.VISIBLE
                } else {
                    binding.studentRecyclerView.visibility = View.VISIBLE
                    binding.emptyStudentMessage.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(loading: Boolean) {
        binding.studentLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
