package com.example.fitnessapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnessapp.data.local.PreferencesManager
import com.example.fitnessapp.data.repository.FitnessRepositoryImpl
import com.example.fitnessapp.domain.service.StepCounterService
import com.example.fitnessapp.domain.service.WorkoutTrackingService
import com.example.fitnessapp.domain.usecase.CalculateActiveTimeUseCase
import com.example.fitnessapp.domain.usecase.CalculateCaloriesUseCase
import com.example.fitnessapp.domain.usecase.CalculateDistanceUseCase
import com.example.fitnessapp.ui.screens.ActiveWorkoutScreen
import com.example.fitnessapp.ui.screens.ProfileScreen
import com.example.fitnessapp.ui.screens.SummaryScreen
import com.example.fitnessapp.ui.screens.WorkoutScreen
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.ui.viewmodel.ProfileViewModel
import com.example.fitnessapp.ui.viewmodel.SummaryViewModel

class MainActivity : ComponentActivity() {

    private var serviceStarted = false
    private var currentRepository: FitnessRepositoryImpl? = null

    // BroadcastReceiver do odbierania aktualizacji kroków z serwisu
    private val stepUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val steps = intent?.getIntExtra(StepCounterService.EXTRA_STEPS, 0) ?: 0
            // Odśwież dane w repository
            currentRepository?.refreshData()
        }
    }

    // Obsługa uprawnień - uruchamiamy serwis DOPIERO po przyznaniu uprawnień
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && !serviceStarted) {
            // Uprawnienie przyznane - uruchom serwis
            startStepCounterService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            // Inicjalizacja zależności
            val prefsManager = PreferencesManager(this)
            val repository = FitnessRepositoryImpl(prefsManager)
            currentRepository = repository

            setContent {
                FitnessAppTheme {
                    FitnessAppContent(
                        repository = repository,
                        prefsManager = prefsManager
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            setContent {
                FitnessAppTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Błąd inicjalizacji: ${e.message}",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Rejestracja BroadcastReceiver
        val filter = IntentFilter(StepCounterService.ACTION_UPDATE_STEPS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stepUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(stepUpdateReceiver, filter)
        }

        // Prośba o uprawnienia gdy aplikacja jest na pierwszym planie
        if (!serviceStarted) {
            requestPermissions()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(stepUpdateReceiver)
        } catch (e: Exception) {
            // Receiver może już być wyrejestrowany
        }
    }

    private fun requestPermissions() {
        // Uprawnienie dla krokomierza (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            startStepCounterService()
        }

        // Uprawnienia GPS (dla treningów)
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Uprawnienie dla notyfikacji (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun startStepCounterService() {
        if (!serviceStarted) {
            try {
                StepCounterService.start(this)
                serviceStarted = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Opcjonalnie: zatrzymaj serwis gdy aplikacja jest zamykana
        // StepCounterService.stop(this)
    }
}

@Composable
fun FitnessAppContent(
    repository: FitnessRepositoryImpl,
    prefsManager: PreferencesManager
) {
    var currentScreen by rememberSaveable { mutableStateOf("summary") }
    var activeWorkoutType by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Inicjalizacja ViewModeli
    val summaryViewModel: SummaryViewModel = viewModel(
        factory = SummaryViewModel.Factory(
            repository = repository,
            calculateDistance = CalculateDistanceUseCase(),
            calculateCalories = CalculateCaloriesUseCase(),
            calculateActiveTime = CalculateActiveTimeUseCase()
        )
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(repository)
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (currentScreen) {
            "summary" -> SummaryScreen(
                viewModel = summaryViewModel,
                onNavigateToWorkout = { currentScreen = "workout" },
                onNavigateToProfile = { currentScreen = "profile" },
                modifier = Modifier.padding(innerPadding)
            )
            "workout" -> WorkoutScreen(
                onNavigateToSummary = { currentScreen = "summary" },
                onStartWorkout = { workoutType ->
                    activeWorkoutType = workoutType
                    WorkoutTrackingService.startWorkout(context, workoutType)
                    currentScreen = "active_workout"
                },
                modifier = Modifier.padding(innerPadding)
            )
            "active_workout" -> ActiveWorkoutScreen(
                workoutType = activeWorkoutType,
                onFinish = { currentScreen = "summary" },
                modifier = Modifier.padding(innerPadding)
            )
            "profile" -> ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { currentScreen = "summary" },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}