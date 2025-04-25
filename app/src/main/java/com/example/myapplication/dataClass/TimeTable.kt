package com.example.myapplication.dataClass

data class Timetable(
    val semester5: Map<String, TimeSlot> = emptyMap(),
    val semester6: Map<String, TimeSlot> = emptyMap()
)