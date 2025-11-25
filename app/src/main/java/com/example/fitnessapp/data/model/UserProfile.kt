package com.example.fitnessapp.data.model

data class UserProfile(
    val gender: String = "Mężczyzna",       // "Mężczyzna" lub "Kobieta"
    val age: Int = 25,
    val weight: Float = 70f,                 // kg
    val height: Int = 175,                   // cm
    val dailyCaloriesGoal: Int = 500,       // cel dzienny kcal (domyślnie 500)
    val dailyStepsGoal: Int = 6000          // cel dzienny kroków (domyślnie 6000)
) {
    // Obliczanie BMR (Basal Metabolic Rate) - podstawowa przemiana materii
    fun calculateBMR(): Float {
        return if (gender == "Mężczyzna") {
            88.362f + (13.397f * weight) + (4.799f * height) - (5.677f * age)
        } else {
            447.593f + (9.247f * weight) + (3.098f * height) - (4.330f * age)
        }
    }

    // Sprawdzenie czy profil jest kompletny
    fun isComplete(): Boolean {
        return age > 0 && weight > 0 && height > 0
    }
}