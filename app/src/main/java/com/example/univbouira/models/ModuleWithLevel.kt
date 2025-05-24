package com.example.univbouira.models

data class ModuleWithLevel(
    val code: String,
    val title: String,
    val semester: Int,
    val level: String,
    val imageUrl: String? = null
)
