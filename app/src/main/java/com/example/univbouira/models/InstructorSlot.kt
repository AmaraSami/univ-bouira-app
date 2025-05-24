// 1) InstructorSlot.kt
package com.example.univbouira.models

data class InstructorSlot(
    val semester: String = "",        // "S1" or "S2"
    val day: String = "",             // e.g. "Saturday"
    val time: String = "",            // e.g. "08:00-09:30"
    val courseCode: String = "",      // e.g. "IHM"
    val room: String = "",            // e.g. "R101"
    val type: String = "",            // "Cour", "TD", or "TP"
    val levels: List<String> = listOf(),  // e.g. listOf("L3")
    val groups: List<String> = listOf()   // e.g. listOf("GROUPE 01"), ignored when type=="Cour"
)
