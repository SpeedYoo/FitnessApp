package com.example.fitnessapp.data.repository

import com.example.fitnessapp.data.model.FitnessData
import com.example.fitnessapp.data.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface FitnessRepository {
    val fitnessData: StateFlow<FitnessData>
    val userProfile: StateFlow<UserProfile>

    fun updateSteps(steps: Int)
    fun updateDistance(distance: Float)
    fun updateCalories(calories: Int)
    fun updateActiveTime(minutes: Int)
    fun saveUserProfile(profile: UserProfile)
    fun resetDailyData()
}

