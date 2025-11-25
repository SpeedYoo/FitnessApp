package com.example.fitnessapp.data.model

data class FitnessData(
    val steps: Int = 0,
    val distance: Float = 0f,              // w kilometrach
    val calories: Int = 0,                  // kcal
    val activeTimeMinutes: Int = 0,         // minuty aktywności
    val lastUpdateTimestamp: Long = System.currentTimeMillis()
)