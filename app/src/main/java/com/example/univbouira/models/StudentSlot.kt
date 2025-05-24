package com.example.univbouira.models

data class StudentSlot(
    val day: String = "",         // e.g. "Saturday"
    val time: String = "",        // e.g. "08:00-09:30"
    val courseCode: String = "",  // was `subject`
    val room: String = "",
    val teacherId: String = "",   // UID of the instructor
    val type: String = ""         // "Cour", "TD", "TP"
)