package com.example.fitnessapp.domain.usecase

class CalculateActiveTimeUseCase {

    private var lastStepCount = 0
    private var lastUpdateTime = System.currentTimeMillis()
    private var totalActiveMinutes = 0

    // Uznajemy aktywność jeśli użytkownik zrobił min 20 kroków w ciągu minuty
    fun execute(currentSteps: Int): Int {
        val currentTime = System.currentTimeMillis()
        val timeDiffMinutes = (currentTime - lastUpdateTime) / (1000 * 60)

        if (timeDiffMinutes >= 1) {
            val stepsDiff = currentSteps - lastStepCount

            // Jeśli użytkownik zrobił przynajmniej 20 kroków w ciągu minuty
            if (stepsDiff >= 20) {
                totalActiveMinutes += timeDiffMinutes.toInt()
            }

            lastStepCount = currentSteps
            lastUpdateTime = currentTime
        }

        return totalActiveMinutes
    }

    fun reset() {
        lastStepCount = 0
        lastUpdateTime = System.currentTimeMillis()
        totalActiveMinutes = 0
    }
}