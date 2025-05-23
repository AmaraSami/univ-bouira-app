package com.example.univbouira

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.univbouira.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView) { view, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBarInsets.bottom)
            insets
        }

        // Read the role from shared preferences
        val sharedPref = getSharedPreferences("StudentPrefs", MODE_PRIVATE)
        val role = sharedPref.getString("role", "")

        val instructorProfileFragment = InstructorProfileFragment()
        val profileFragment = ProfileFragment()
        val notesFragment = NotesFragment()
        val timetableFragment = TimeTableFragment()
        val coursesFragment = CoursesFragment()
        val modulesFragment = ModulesFragment()

        // Redirect based on role
        val defaultFragment = if (role == "student") coursesFragment else modulesFragment
        makeCurrentFragment(defaultFragment)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    if (role == "instructor") {
                        makeCurrentFragment(instructorProfileFragment)
                    } else {
                        makeCurrentFragment(profileFragment)
                    }
                }
                R.id.notes -> makeCurrentFragment(notesFragment)
                R.id.timetable -> makeCurrentFragment(timetableFragment)
                R.id.courses -> {
                    if (role == "instructor") {
                        makeCurrentFragment(modulesFragment)
                    } else {
                        makeCurrentFragment(coursesFragment)
                    }
                }
            }
            true
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
    }
}
