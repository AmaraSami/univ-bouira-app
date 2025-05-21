package com.example.univbouira.models

data class UploadedFile(
    var id: String? = null,           // Firestore document ID
    val name: String = "",
    val url: String = "",
    val timestamp: Long = 0L,
    val moduleCode: String = "",
    val moduleTitle: String = ""
)
