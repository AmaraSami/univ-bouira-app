package com.example.univbouira.models

data class LearningCourse(
    val name: String = "",
    val room: String = "",
    val professor: String = "",
    val level: String? = null,
    val type: String = "" ,// lecture, TD, TP, etc.
    val semester: Int = 1
)
