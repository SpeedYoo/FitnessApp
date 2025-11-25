package com.example.fitnessapp.data.repository

import com.example.fitnessapp.data.local.PreferencesManager
import com.example.fitnessapp.data.model.FitnessData
import com.example.fitnessapp.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FitnessRepositoryImpl(
    private val prefsManager: PreferencesManager
) : FitnessRepository {

    private val _fitnessData = MutableStateFlow(prefsManager.getFitnessData())
    override val fitnessData: StateFlow<FitnessData> = _fitnessData.asStateFlow()

    private val _userProfile = MutableStateFlow(prefsManager.getUserProfile())
    override val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        // Sprawdź czy to nowy dzień i zresetuj dane jeśli tak
        if (prefsManager.isNewDay()) {
            resetDailyData()
        }
    }

    override fun updateSteps(steps: Int) {
        val currentData = _fitnessData.value
        val updatedData = currentData.copy(
            steps = steps,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        _fitnessData.value = updatedData
        prefsManager.saveFitnessData(updatedData)
    }

    override fun updateDistance(distance: Float) {
        val currentData = _fitnessData.value
        val updatedData = currentData.copy(
            distance = distance,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        _fitnessData.value = updatedData
        prefsManager.saveFitnessData(updatedData)
    }

    override fun updateCalories(calories: Int) {
        val currentData = _fitnessData.value
        val updatedData = currentData.copy(
            calories = calories,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        _fitnessData.value = updatedData
        prefsManager.saveFitnessData(updatedData)
    }

    override fun updateActiveTime(minutes: Int) {
        val currentData = _fitnessData.value
        val updatedData = currentData.copy(
            activeTimeMinutes = minutes,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        _fitnessData.value = updatedData
        prefsManager.saveFitnessData(updatedData)
    }

    override fun saveUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        prefsManager.saveUserProfile(profile)
    }

    override fun resetDailyData() {
        val resetData = FitnessData()
        _fitnessData.value = resetData
        prefsManager.saveFitnessData(resetData)
    }
}