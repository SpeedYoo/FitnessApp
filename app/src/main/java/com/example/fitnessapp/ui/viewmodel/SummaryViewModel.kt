package com.example.fitnessapp.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.fitnessapp.data.model.FitnessData
import com.example.fitnessapp.data.model.UserProfile
import com.example.fitnessapp.data.repository.FitnessRepository
import com.example.fitnessapp.domain.usecase.CalculateActiveTimeUseCase
import com.example.fitnessapp.domain.usecase.CalculateCaloriesUseCase
import com.example.fitnessapp.domain.usecase.CalculateDistanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SummaryUiState(
    val steps: Int = 0,
    val stepsGoal: Int = 6000,
    val distance: String = "0,0 Km",
    val calories: Int = 0,
    val caloriesGoal: Int = 500,
    val activeTimeMinutes: Int = 0,
    val lastWorkoutDistance: String = "0,0 Km"
)

class SummaryViewModel(
    private val repository: FitnessRepository,
    private val calculateDistance: CalculateDistanceUseCase,
    private val calculateCalories: CalculateCaloriesUseCase,
    private val calculateActiveTime: CalculateActiveTimeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.fitnessData.collect { fitnessData ->
                repository.userProfile.collect { userProfile ->
                    updateUiState(fitnessData, userProfile)
                }
            }
        }
    }

    private fun updateUiState(fitnessData: FitnessData, userProfile: UserProfile) {
        _uiState.value = SummaryUiState(
            steps = fitnessData.steps,
            stepsGoal = userProfile.dailyStepsGoal,
            distance = formatDistance(fitnessData.distance),
            calories = fitnessData.calories,
            caloriesGoal = userProfile.dailyCaloriesGoal,
            activeTimeMinutes = fitnessData.activeTimeMinutes,
            lastWorkoutDistance = "217,2 Km"
        )
    }

    // Wywoływane przez StepCounterService gdy wykryje nowe kroki
    fun onStepsUpdated(newSteps: Int) {
        viewModelScope.launch {
            val profile = repository.userProfile.value

            // Oblicz dystans
            val distance = calculateDistance.execute(
                newSteps,
                profile.height,
                profile.gender
            )

            // Oblicz czas aktywności
            val activeTime = calculateActiveTime.execute(newSteps)

            // Oblicz kalorie
            val calories = calculateCalories.execute(
                newSteps,
                distance,
                activeTime,
                profile
            )

            // Zaktualizuj repository
            repository.updateSteps(newSteps)
            repository.updateDistance(distance)
            repository.updateCalories(calories)
            repository.updateActiveTime(activeTime)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatDistance(distanceKm: Float): String {
        return String.format("%.1f Km", distanceKm).replace('.', ',')
    }

    // Factory do tworzenia ViewModela
    class Factory(
        private val repository: FitnessRepository,
        private val calculateDistance: CalculateDistanceUseCase,
        private val calculateCalories: CalculateCaloriesUseCase,
        private val calculateActiveTime: CalculateActiveTimeUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return SummaryViewModel(
                repository,
                calculateDistance,
                calculateCalories,
                calculateActiveTime
            ) as T
        }
    }
}