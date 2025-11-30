package com.example.fitnessapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.example.fitnessapp.data.local.PreferencesManager
import com.example.fitnessapp.data.repository.FitnessRepositoryImpl
import com.example.fitnessapp.service.StepCounterService
import com.example.fitnessapp.ui.navigation.FitnessApp
import com.example.fitnessapp.ui.navigation.Screen
import com.example.fitnessapp.ui.navigation.isWorkoutActive
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.utils.PermissionHandler

class MainActivity : ComponentActivity() {

    private lateinit var permissionHandler: PermissionHandler
    private lateinit var repository: FitnessRepositoryImpl
    private lateinit var prefsManager: PreferencesManager

    private var stepCounterStarted = false

    private val stepUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            repository.refreshData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initializeDependencies()
        setupPermissionHandler()

        val startScreen = determineStartScreen(intent)

        setContent {
            FitnessAppTheme {
                FitnessApp(
                    repository = repository,
                    prefsManager = prefsManager,
                    startDestination = startScreen
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Jeśli kliknięto powiadomienie treningu, odśwież UI
        if (intent.action == ACTION_OPEN_ACTIVE_WORKOUT && isWorkoutActive(this)) {
            recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        registerStepReceiver()
        permissionHandler.requestPermissions()
    }

    override fun onPause() {
        super.onPause()
        unregisterStepReceiver()
    }

    private fun initializeDependencies() {
        prefsManager = PreferencesManager(this)
        repository = FitnessRepositoryImpl(prefsManager)
    }

    private fun setupPermissionHandler() {
        permissionHandler = PermissionHandler(this) {
            startStepCounterIfNeeded()
        }
    }

    /**
     * Określa ekran startowy na podstawie intencji i stanu aplikacji
     */
    private fun determineStartScreen(intent: Intent?): Screen {
        // Sprawdź czy otwarto z powiadomienia treningu
        if (intent?.action == ACTION_OPEN_ACTIVE_WORKOUT) {
            return Screen.ActiveWorkout
        }

        // Sprawdź czy trening jest aktywny (np. po zamknięciu i ponownym otwarciu)
        if (isWorkoutActive(this)) {
            return Screen.ActiveWorkout
        }

        return Screen.Summary
    }

    private fun startStepCounterIfNeeded() {
        if (!stepCounterStarted && PermissionHandler.hasActivityRecognition(this)) {
            StepCounterService.start(this)
            stepCounterStarted = true
        }
    }

    private fun registerStepReceiver() {
        val filter = IntentFilter(StepCounterService.ACTION_UPDATE_STEPS)
        ContextCompat.registerReceiver(
            this,
            stepUpdateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unregisterStepReceiver() {
        try {
            unregisterReceiver(stepUpdateReceiver)
        } catch (e: IllegalArgumentException) {
        }
    }

    companion object {
        const val ACTION_OPEN_ACTIVE_WORKOUT = "OPEN_ACTIVE_WORKOUT"
    }
}