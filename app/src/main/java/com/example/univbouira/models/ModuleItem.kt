package com.example.univbouira.models

data class ModuleItem(
    val code: String = "",
    val title: String = "",
    val semester: Int = 0      // Added semester field for filtering
)
