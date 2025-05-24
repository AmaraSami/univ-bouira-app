package com.example.univbouira.models

data class NotesItems(
    val code: String,
    val title: String,
    val description: String,
    val tp: String = "-",
    val td: String = "-",
    val exam: String = "-",
    val moyenne : String = "-"
)
