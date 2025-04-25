package com.example.myapplication.dataClass

data class TimeSlot(
    val day: String,     // e.g. "Mon"
    val time: String,    // e.g. "08:00"
    val subject: String,
    val room: String,
    val teacher: String,
    val type: String
)


