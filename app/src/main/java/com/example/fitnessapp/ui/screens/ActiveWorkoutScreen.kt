package com.example.fitnessapp.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.fitnessapp.domain.service.WorkoutTrackingService

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
            .background(Color(0xFF000000))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Typ treningu
        Text(
            text = workoutType,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Duży timer
        Surface(
            modifier = Modifier.size(200.dp),
            color = Color(0xFF2C2C2E),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = formatDuration(duration / 1000),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF32D74B)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Statystyki
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WorkoutStat(
                label = "Dystans",
                value = String.format("%.2f", distance),
                unit = "km"
            )

            WorkoutStat(
                label = "Kalorie",
                value = calories.toString(),
                unit = "kcal"
            )

            WorkoutStat(
                label = "Tempo",
                value = if (distance > 0 && duration > 0) {
                    val pace = (duration / 1000 / 60) / distance // min/km
                    String.format("%.1f", pace)
                } else {
                    "0.0"
                },
                unit = "min/km"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Przyciski kontroli
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Przycisk Stop
            Button(
                onClick = {
                    WorkoutTrackingService.stopWorkout(context)
                    onFinish()
                },
                modifier = Modifier.size(70.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF3B30)
                ),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }

            // Przycisk Pauza/Wznów
            Button(
                onClick = {
                    if (isPaused) {
                        WorkoutTrackingService.resumeWorkout(context)
                        isPaused = false
                    } else {
                        WorkoutTrackingService.pauseWorkout(context)
                        isPaused = true
                    }
                },
                modifier = Modifier.size(90.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF32D74B)
                ),
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

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun WorkoutStat(
    label: String,
    value: String,
    unit: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2E)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = unit,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}