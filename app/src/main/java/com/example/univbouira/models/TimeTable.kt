package com.example.univbouira.models

data class Timetable(
    val semester5: Map<String, TimeSlot> = emptyMap(),
    val semester6: Map<String, TimeSlot> = emptyMap()
)