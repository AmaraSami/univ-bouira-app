package com.example.univbouira.models

data class CourseMaterial(
    val title: String = "",
    val type: String = "", // "lecture", "td", "tp"
    val fileUrl: String = "",
    val fileName: String = ""
)
