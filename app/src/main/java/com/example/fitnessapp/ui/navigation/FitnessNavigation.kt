package com.example.fitnessapp.ui.navigation

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnessapp.data.local.PreferencesManager
import com.example.fitnessapp.data.repository.FitnessRepositoryImpl
import com.example.fitnessapp.service.WorkoutTrackingService
import com.example.fitnessapp.ui.screens.*
import com.example.fitnessapp.ui.viewmodel.ProfileViewModel
import com.example.fitnessapp.ui.viewmodel.SummaryViewModel

/**
 * Sealed class reprezentująca ekrany aplikacji
 */
sealed class Screen(val route: String) {
    data object Summary : Screen("summary")
    data object Workout : Screen("workout")
    data object ActiveWorkout : Screen("active_workout")
    data object Profile : Screen("profile")
    data object History : Screen("history")
    data class WorkoutDetail(val workoutId: Int) : Screen("workout_detail")
}

/**
 * Główny komponent nawigacji aplikacji
 */
@Composable
fun FitnessApp(
    repository: FitnessRepositoryImpl,
    prefsManager: PreferencesManager,
    startDestination: Screen = Screen.Summary,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Stan nawigacji
    var currentScreen by rememberSaveable { mutableStateOf(startDestination.route) }
    var activeWorkoutType by rememberSaveable { mutableStateOf("") }
    var selectedWorkoutId by rememberSaveable { mutableIntStateOf(0) }

    // Sprawdź czy trening jest aktywny przy starcie
    LaunchedEffect(startDestination) {
        if (startDestination is Screen.ActiveWorkout || currentScreen == Screen.ActiveWorkout.route) {
            activeWorkoutType = getActiveWorkoutType(context)
        }
    }

    // ViewModels
    val summaryViewModel: SummaryViewModel = viewModel(
        factory = SummaryViewModel.Factory(
            repository = repository,
        )
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(repository)
    )

    // Nawigacja
    when (currentScreen) {
        Screen.Summary.route -> SummaryScreen(
            viewModel = summaryViewModel,
            onNavigateToWorkout = { currentScreen = Screen.Workout.route },
            onNavigateToProfile = { currentScreen = Screen.Profile.route },
            onNavigateToHistory = { currentScreen = Screen.History.route },
            modifier = modifier
        )

        Screen.Workout.route -> WorkoutScreen(
            onNavigateToSummary = { currentScreen = Screen.Summary.route },
            onStartWorkout = { workoutType ->
                activeWorkoutType = workoutType
                WorkoutTrackingService.startWorkout(context, workoutType)
                currentScreen = Screen.ActiveWorkout.route
            },
            modifier = modifier
        )

        Screen.ActiveWorkout.route -> ActiveWorkoutScreen(
            workoutType = activeWorkoutType,
            onFinish = { currentScreen = Screen.Summary.route },
            modifier = modifier
        )

        Screen.Profile.route -> ProfileScreen(
            viewModel = profileViewModel,
            onNavigateBack = { currentScreen = Screen.Summary.route },
            modifier = modifier
        )

        Screen.History.route -> WorkoutHistoryScreen(
            onNavigateBack = { currentScreen = Screen.Summary.route },
            onWorkoutClick = { workoutId ->
                selectedWorkoutId = workoutId
                currentScreen = "workout_detail"
            },
            modifier = modifier
        )

        "workout_detail" -> WorkoutDetailScreen(
            workoutId = selectedWorkoutId,
            onNavigateBack = { currentScreen = Screen.History.route },
            modifier = modifier
        )
    }
}

/**
 * Pobiera typ aktywnego treningu z SharedPreferences
 */
private fun getActiveWorkoutType(context: Context): String {
    val prefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
    return prefs.getString("current_workout_type", "Outdoor Walk") ?: "Outdoor Walk"
}

/**
 * Sprawdza czy trening jest aktualnie aktywny
 */
fun isWorkoutActive(context: Context): Boolean {
    val prefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("workout_active", false)
}