package com.example.fitnessapp.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.fitnessapp.data.model.FitnessData
import com.example.fitnessapp.data.model.UserProfile
import com.example.fitnessapp.data.repository.FitnessRepository
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
)

class SummaryViewModel(
    private val repository: FitnessRepository,
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
        )
    }

    // Wywoływane przez StepCounterService gdy wykryje nowe kroki

    @SuppressLint("DefaultLocale")
    private fun formatDistance(distanceKm: Float): String {
        return String.format("%.1f Km", distanceKm).replace('.', ',')
    }

    class Factory(
        private val repository: FitnessRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return SummaryViewModel(
                repository,
            ) as T
        }
    }
}