package com.example.fitnessapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.fitnessapp.data.model.FitnessData
import com.example.fitnessapp.data.model.UserProfile

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)

    // === PROFIL UŻYTKOWNIKA ===

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString(KEY_GENDER, profile.gender)
            putInt(KEY_AGE, profile.age)
            putFloat(KEY_WEIGHT, profile.weight)
            putInt(KEY_HEIGHT, profile.height)
            putInt(KEY_DAILY_CALORIES_GOAL, profile.dailyCaloriesGoal)
            putInt(KEY_DAILY_STEPS_GOAL, profile.dailyStepsGoal)
            apply()
        }
    }

    fun getUserProfile(): UserProfile {
        return UserProfile(
            gender = prefs.getString(KEY_GENDER, "Mężczyzna") ?: "Mężczyzna",
            age = prefs.getInt(KEY_AGE, 25),
            weight = prefs.getFloat(KEY_WEIGHT, 70f),
            height = prefs.getInt(KEY_HEIGHT, 175),
            dailyCaloriesGoal = prefs.getInt(KEY_DAILY_CALORIES_GOAL, 500),
            dailyStepsGoal = prefs.getInt(KEY_DAILY_STEPS_GOAL, 6000)
        )
    }

    // === DANE FITNESS ===

    fun saveFitnessData(data: FitnessData) {
        prefs.edit().apply {
            putInt(KEY_STEPS, data.steps)
            putFloat(KEY_DISTANCE, data.distance)
            putInt(KEY_CALORIES, data.calories)
            putInt(KEY_ACTIVE_TIME, data.activeTimeMinutes)
            putLong(KEY_LAST_UPDATE, data.lastUpdateTimestamp)
            apply()
        }
    }

    fun getFitnessData(): FitnessData {
        return FitnessData(
            steps = prefs.getInt(KEY_STEPS, 0),
            distance = prefs.getFloat(KEY_DISTANCE, 0f),
            calories = prefs.getInt(KEY_CALORIES, 0),
            activeTimeMinutes = prefs.getInt(KEY_ACTIVE_TIME, 0),
            lastUpdateTimestamp = prefs.getLong(KEY_LAST_UPDATE, System.currentTimeMillis())
        )
    }

    // Resetowanie danych na nowy dzień
    fun resetDailyData() {
        saveFitnessData(FitnessData())
    }

    // Sprawdzanie czy to nowy dzień
    fun isNewDay(): Boolean {
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0)
        val currentDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val lastDay = lastUpdate / (1000 * 60 * 60 * 24)
        return currentDay > lastDay
    }

    companion object {
        // Keys dla profilu
        private const val KEY_GENDER = "gender"
        private const val KEY_AGE = "age"
        private const val KEY_WEIGHT = "weight"
        private const val KEY_HEIGHT = "height"
        private const val KEY_DAILY_CALORIES_GOAL = "daily_calories_goal"
        private const val KEY_DAILY_STEPS_GOAL = "daily_steps_goal"

        // Keys dla danych fitness
        private const val KEY_STEPS = "steps"
        private const val KEY_DISTANCE = "distance"
        private const val KEY_CALORIES = "calories"
        private const val KEY_ACTIVE_TIME = "active_time"
        private const val KEY_LAST_UPDATE = "last_update"
    }
}