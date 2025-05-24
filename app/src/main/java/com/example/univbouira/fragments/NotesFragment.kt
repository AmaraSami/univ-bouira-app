package com.example.univbouira.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.univbouira.R
import com.example.univbouira.adapters.NotesAdapter
import com.example.univbouira.models.NotesItem
import com.example.univbouira.ui.ModuleNotesDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotesFragment : Fragment() {

    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var semester1Button: Button
    private lateinit var semester2Button: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notesList = mutableListOf<NotesItem>()
    private lateinit var notesAdapter: NotesAdapter

    private var selectedSemester: Int = 1 // 1 or 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        notesRecyclerView = view.findViewById(R.id.recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyView = view.findViewById(R.id.emptyNotesMessage)
        semester1Button = view.findViewById(R.id.semester1Button)
        semester2Button = view.findViewById(R.id.semester2Button)

        notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notesAdapter = NotesAdapter(notesList) { item ->
            val intent = Intent(requireContext(), ModuleNotesDetailActivity::class.java)
            intent.putExtra("moduleCode", item.code)
            intent.putExtra("moduleTitle", item.moduleName)
            startActivity(intent)
        }

        notesRecyclerView.adapter = notesAdapter

        semester1Button.setOnClickListener {
            selectedSemester = 1
            updateButtonColors()
            loadCoursesAndGrades()
        }

        semester2Button.setOnClickListener {
            selectedSemester = 2
            updateButtonColors()
            loadCoursesAndGrades()
        }

        updateButtonColors()
        loadCoursesAndGrades()

        return view
    }

    private fun updateButtonColors() {
        val selectedColor = Color.parseColor("#007BA7") // Blue
        val defaultColor = Color.parseColor("#BDBDBD")  // Gray

        semester1Button.setBackgroundColor(if (selectedSemester == 1) selectedColor else defaultColor)
        semester2Button.setBackgroundColor(if (selectedSemester == 2) selectedColor else defaultColor)
    }

    private fun loadCoursesAndGrades() {
        progressBar.visibility = View.VISIBLE
        notesList.clear()
        notesAdapter.notifyDataSetChanged()

        val user = auth.currentUser
        val userEmail = user?.email ?: return

        db.collection("students").whereEqualTo("email", userEmail).get()
            .addOnSuccessListener { studentDocs ->
                if (studentDocs.isEmpty) {
                    showEmptyView()
                    return@addOnSuccessListener
                }

                val studentDoc = studentDocs.first()
                val studentId = studentDoc.id
                val level = studentDoc.getString("level") ?: "L3"
                val groupName = studentDoc.getString("groupName") ?: "GROUPE 01"

                val sharedPref = requireContext().getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE)
                sharedPref.edit().apply {
                    putString("cardId", studentId)
                    putString("groupName", groupName)
                    apply()
                }


                val coursesRef = db.collection("levels")
                    .document(level)
                    .collection("courses")

                coursesRef.get()
                    .addOnSuccessListener { coursesSnapshot ->
                        Log.d("GradesDebug", "Found ${coursesSnapshot.size()} courses")

                        if (coursesSnapshot.isEmpty) {
                            showEmptyView()
                            return@addOnSuccessListener
                        }

                        val gradeTasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                        for (courseDoc in coursesSnapshot) {
                            val courseTitle = courseDoc.getString("title") ?: continue
                            val courseCode = courseDoc.getString("code") ?: continue
                            val semester = courseDoc.getLong("semester")?.toInt() ?: 1

                            // 🟡 Only include courses for the selected semester
                            if (semester != selectedSemester) continue

                            val gradeRef = db.collection("grades")
                                .document(courseCode)
                                .collection(groupName)
                                .document(studentId)

                            val task = gradeRef.get().addOnSuccessListener { gradeDoc ->
                                if (gradeDoc.exists()) {
                                    val moyenne = gradeDoc.getDouble("moyenne")
                                    Log.d("GradesDebug", "Grade for $courseCode: $moyenne")

                                    if (moyenne != null) {
                                        val noteItem = NotesItem(code = courseCode, moduleName = courseTitle, moyenne = moyenne)
                                        notesList.add(noteItem)
                                    } else {
                                        Log.d("GradesDebug", "Moyenne is null for $courseCode")
                                    }
                                } else {
                                    Log.d("GradesDebug", "No grade doc for $courseCode")
                                }
                            }

                            gradeTasks.add(task)
                        }

                        // 🟢 Wait for all grade fetches to finish
                        if (gradeTasks.isEmpty()) {
                            updateUIAfterLoad()
                            return@addOnSuccessListener
                        }

                        com.google.android.gms.tasks.Tasks.whenAllSuccess<Any>(gradeTasks)
                            .addOnSuccessListener {
                                Log.d("GradesDebug", "All grades loaded. Final size: ${notesList.size}")
                                notesAdapter.notifyDataSetChanged()
                                updateUIAfterLoad()
                            }
                            .addOnFailureListener {
                                Log.e("GradesDebug", "Error loading some grades", it)
                                showEmptyView()
                            }
                    }
                    .addOnFailureListener {
                        showEmptyView()
                    }
            }
            .addOnFailureListener {
                showEmptyView()
            }
    }




    private fun updateUIAfterLoad() {
        progressBar.visibility = View.GONE
        if (notesList.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            notesRecyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            notesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showEmptyView() {
        progressBar.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        notesRecyclerView.visibility = View.GONE
    }
}
