package com.example.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.myapplication.fragments.*
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
        val profileFragment = ProfileFragment()
        val notesFragment = NotesFragment()
        val timetableFragment = TimeTableFragment()
        val coursesFragment = CoursesFragment()

        makeCurrentFragment(coursesFragment)


        bottomNavigationView.setOnItemSelectedListener {menuItem ->
            when(menuItem.itemId) {
                R.id.profile -> makeCurrentFragment(profileFragment)
                R.id.notes -> makeCurrentFragment(notesFragment)
                R.id.timetable -> makeCurrentFragment(timetableFragment)
                R.id.courses -> makeCurrentFragment(coursesFragment)
            }
            true
        }


    }

    private fun makeCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }

    }

}