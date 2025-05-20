package com.example.univbouira.models

data class UploadedFile(
    val id: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val moduleName: String = "",
    val uploadTimestamp: Long = 0L
)
