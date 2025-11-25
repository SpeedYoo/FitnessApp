package com.example.fitnessapp.domain.usecase

import com.example.fitnessapp.data.model.UserProfile

class CalculateCaloriesUseCase {

    fun execute(
        steps: Int,
        distanceKm: Float,
        activeTimeMinutes: Int,
        profile: UserProfile
    ): Int {
        if (!profile.isComplete()) return 0

        // MET (Metabolic Equivalent of Task) dla chodzenia
        val walkingMET = when {
            steps < 3000 -> 2.0f      // Powolne chodzenie
            steps < 7000 -> 3.5f      // Normalne chodzenie
            else -> 5.0f               // Szybkie chodzenie
        }

        // Kalorie = MET × waga (kg) × czas (h)
        val timeInHours = activeTimeMinutes / 60f
        val caloriesBurned = walkingMET * profile.weight * timeInHours

        return caloriesBurned.toInt()
    }

    // Alternatywna formuła bazująca na dystansie
    fun executeByDistance(distanceKm: Float, profile: UserProfile): Int {
        if (!profile.isComplete()) return 0

        // ~0.57 kcal/kg/km dla chodzenia
        val caloriesPerKm = 0.57f * profile.weight
        return (caloriesPerKm * distanceKm).toInt()
    }
}