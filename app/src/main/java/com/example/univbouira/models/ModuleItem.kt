package com.example.univbouira.models

data class ModuleItem(
    val code: String = "",
    val title: String = "",
    val subtitle: String = "",     // e.g. "Android Development"
    val note: String = "N/A",     // Optional note, default "N/A"
    val semester: String = ""      // Added semester field for filtering
)
