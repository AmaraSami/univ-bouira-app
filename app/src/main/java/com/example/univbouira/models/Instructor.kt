package com.example.univbouira.models

data class Instructor(
    val fullName: String = "",
    val email: String = "",
    val imageUrl: String = "",
    val assignedCourses: List<String> = emptyList()
)

