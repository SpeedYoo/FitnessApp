package com.example.fitnessapp.domain.usecase

class CalculateDistanceUseCase {

    // Średnia długość kroku: ~0.78m dla mężczyzn, ~0.70m dla kobiet
    fun execute(steps: Int, userHeight: Int, gender: String): Float {
        val strideLength = if (gender == "Mężczyzna") {
            userHeight * 0.415f / 100 // w metrach
        } else {
            userHeight * 0.413f / 100
        }

        val distanceMeters = steps * strideLength
        return distanceMeters / 1000 // konwersja na km
    }
}