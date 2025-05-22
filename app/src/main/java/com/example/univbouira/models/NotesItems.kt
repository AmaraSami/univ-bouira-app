package com.example.univbouira.models

data class NotesItems (
    val code: String = "",
    val title: String = "",
    val subtitle: String = "",     // e.g. "Android Development"
    val note: String = "N/A",     // Optional note, default "N/A"
    val semester: Int = 0      // Added semester field for filtering
)