package com.example.fitnessapp.data.model

import java.util.Date

data class WorkoutHistory(
    val id: Long = System.currentTimeMillis(),
    val workoutType: String,
    val date: Date = Date(),
    val durationMinutes: Int,
    val distance: Float,
    val calories: Int,
    val steps: Int
)