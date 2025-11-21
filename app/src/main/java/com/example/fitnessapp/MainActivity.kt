package com.example.fitnessapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.fitnessapp.ui.screens.ProfileScreen
import com.example.fitnessapp.ui.screens.SummaryScreen
import com.example.fitnessapp.ui.screens.WorkoutScreen
import com.example.fitnessapp.ui.theme.FitnessAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                FitnessAppApp()
            }
        }
    }
}

@Composable
fun FitnessAppApp() {
    var currentScreen by rememberSaveable { mutableStateOf("summary") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (currentScreen) {
            "summary" -> SummaryScreen(
                onNavigateToWorkout = { currentScreen = "workout" },
                onNavigateToProfile = { currentScreen = "profile" },
                modifier = Modifier.padding(innerPadding)
            )
            "workout" -> WorkoutScreen(
                onNavigateToSummary = { currentScreen = "summary" },
                modifier = Modifier.padding(innerPadding)
            )
            "profile" -> ProfileScreen(
                onNavigateBack = { currentScreen = "summary" },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}