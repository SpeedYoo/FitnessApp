package com.example.fitnessapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.fitnessapp.data.model.UserProfile
import com.example.fitnessapp.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow(repository.userProfile.value)
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun updateProfile(
        gender: String,
        age: Int,
        weight: Float,
        height: Int,
        caloriesGoal: Int,
        stepsGoal: Int
    ) {
        val updatedProfile = UserProfile(
            gender = gender,
            age = age,
            weight = weight,
            height = height,
            dailyCaloriesGoal = caloriesGoal,
            dailyStepsGoal = stepsGoal
        )

        _userProfile.value = updatedProfile
        repository.saveUserProfile(updatedProfile)
    }

    // Factory do tworzenia ViewModela
    class Factory(
        private val repository: FitnessRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return ProfileViewModel(repository) as T
        }
    }
}