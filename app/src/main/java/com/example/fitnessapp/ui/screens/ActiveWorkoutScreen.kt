package com.example.fitnessapp.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.fitnessapp.domain.service.WorkoutTrackingService
import com.example.fitnessapp.ui.components.WorkoutStatCard
import com.example.fitnessapp.ui.theme.*
import com.example.fitnessapp.ui.utils.FitnessUtils

@Composable
fun ActiveWorkoutScreen(
    workoutType: String,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var duration by remember { mutableLongStateOf(0L) }
    var distance by remember { mutableFloatStateOf(0f) }
    var calories by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }

    // BroadcastReceiver do odbierania aktualizacji z Service
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                duration = intent?.getLongExtra(WorkoutTrackingService.EXTRA_DURATION, 0L) ?: 0L
                distance = intent?.getFloatExtra(WorkoutTrackingService.EXTRA_DISTANCE, 0f) ?: 0f
                calories = intent?.getIntExtra(WorkoutTrackingService.EXTRA_CALORIES, 0) ?: 0
            }
        }

        val filter = IntentFilter(WorkoutTrackingService.ACTION_WORKOUT_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .padding(Dimensions.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Typ treningu
        Text(
            text = workoutType,
            style = FitnessTextStyles.screenTitle,
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Duży timer
        TimerDisplay(duration = duration)

        Spacer(modifier = Modifier.height(48.dp))

        // Statystyki treningu
        WorkoutStats(
            distance = distance,
            calories = calories,
            duration = duration
        )

        Spacer(modifier = Modifier.weight(1f))

        // Przyciski kontroli
        WorkoutControls(
            isPaused = isPaused,
            onStop = {
                WorkoutTrackingService.stopWorkout(context)
                onFinish()
            },
            onPauseResume = {
                if (isPaused) {
                    WorkoutTrackingService.resumeWorkout(context)
                    isPaused = false
                } else {
                    WorkoutTrackingService.pauseWorkout(context)
                    isPaused = true
                }
            }
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun TimerDisplay(duration: Long) {
    Surface(
        modifier = Modifier.size(Dimensions.timerSize),
        color = SurfaceDark,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = FitnessUtils.formatDurationFromSeconds(duration / 1000),
                fontSize = 40.sp,
                style = FitnessTextStyles.statisticValueLarge,
                color = FitnessGreen
            )
        }
    }
}

@Composable
private fun WorkoutStats(
    distance: Float,
    calories: Int,
    duration: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WorkoutStatCard(
            label = "Dystans",
            value = FitnessUtils.formatDecimal(distance),
            unit = "km"
        )

        WorkoutStatCard(
            label = "Kalorie",
            value = calories.toString(),
            unit = "kcal"
        )

        WorkoutStatCard(
            label = "Tempo",
            value = calculateCurrentPace(distance, duration),
            unit = "min/km"
        )
    }
}

@Composable
private fun WorkoutControls(
    isPaused: Boolean,
    onStop: () -> Unit,
    onPauseResume: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Przycisk Stop
        Button(
            onClick = onStop,
            modifier = Modifier.size(70.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FitnessRed),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop",
                modifier = Modifier.size(32.dp),
                tint = TextWhite
            )
        }

        // Przycisk Pauza/Wznów
        Button(
            onClick = onPauseResume,
            modifier = Modifier.size(90.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FitnessGreen),
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) "Wznów" else "Pauza",
                modifier = Modifier.size(40.dp),
                tint = Color.Black
            )
        }
    }
}

/**
 * Oblicza aktualne tempo
 */
private fun calculateCurrentPace(distance: Float, duration: Long): String {
    return if (distance > 0 && duration > 0) {
        val pace = (duration / 1000 / 60) / distance // min/km
        String.format("%.1f", pace)
    } else {
        "0.0"
    }
}