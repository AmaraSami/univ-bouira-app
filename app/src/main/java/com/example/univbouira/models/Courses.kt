package com.example.univbouira.models

data class Course(
    val name: String = "",
    val room: String = "",
    val professor: String = "",
    val type: String = "" // e.g., "lecture", "lab", "tutorial"
)