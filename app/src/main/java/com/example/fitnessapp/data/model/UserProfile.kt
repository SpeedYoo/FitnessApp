package com.example.fitnessapp.data.model

data class UserProfile(
    val gender: String = "Mężczyzna",       // "Mężczyzna" lub "Kobieta"
    val age: Int = 25,
    val weight: Float = 70f,                 // kg
    val height: Int = 175,                   // cm
    val dailyCaloriesGoal: Int = 500,       // cel dzienny kcal (domyślnie 500)
    val dailyStepsGoal: Int = 6000          // cel dzienny kroków (domyślnie 6000)
) {

    // Sprawdzenie czy profil jest kompletny
    fun isComplete(): Boolean {
        return age > 0 && weight > 0 && height > 0
    }
}