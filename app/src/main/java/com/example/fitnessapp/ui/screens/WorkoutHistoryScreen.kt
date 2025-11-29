package com.example.fitnessapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WorkoutHistoryItem(
    val id: Int,
    val type: String,
    val timestamp: Long,
    val duration: Long, // minuty
    val distance: Float, // km
    val calories: Int
)

@Composable
fun WorkoutHistoryScreen(
    onNavigateBack: () -> Unit,
    onWorkoutClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)

    // Stan odświeżania - zmienia się gdy ekran jest tworzony
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        refreshTrigger++
    }

    // Załaduj historię treningów - odświeży się gdy refreshTrigger się zmieni
    val workouts by remember(refreshTrigger) {
        derivedStateOf {
            val workoutCount = prefs.getInt("workout_count", 0)
            val list = mutableListOf<WorkoutHistoryItem>()

            android.util.Log.d("WorkoutHistory", "Total workouts in database: $workoutCount")

            for (i in workoutCount downTo 1) { // Od najnowszych
                // Sprawdź czy nie został usunięty
                val isDeleted = prefs.getBoolean("workout_${i}_deleted", false)
                if (isDeleted) {
                    android.util.Log.d("WorkoutHistory", "Workout $i is deleted, skipping")
                    continue
                }

                val type = prefs.getString("workout_${i}_type", "Unknown") ?: "Unknown"
                val timestamp = prefs.getLong("workout_${i}_timestamp", 0)
                val duration = prefs.getLong("workout_${i}_duration", 0)
                val distance = prefs.getFloat("workout_${i}_distance", 0f)
                val calories = prefs.getInt("workout_${i}_calories", 0)

                android.util.Log.d("WorkoutHistory", "Workout $i: type=$type, duration=$duration min, distance=$distance km, calories=$calories")

                list.add(WorkoutHistoryItem(i, type, timestamp, duration, distance, calories))
            }

            android.util.Log.d("WorkoutHistory", "Loaded ${list.size} workouts")
            list
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        // Nagłówek
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Powrót",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Historia treningów",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (workouts.isEmpty()) {
            // Pusty stan
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🏃",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Brak treningów",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Rozpocznij swój pierwszy trening!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Lista treningów
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workouts.size) { index ->
                    WorkoutHistoryCard(
                        workout = workouts[index],
                        onClick = { onWorkoutClick(workouts[index].id) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryCard(
    workout: WorkoutHistoryItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2E)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lewa strona - typ i data
            Column {
                Text(
                    text = getWorkoutEmoji(workout.type),
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = workout.type,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = formatDate(workout.timestamp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Prawa strona - statystyki
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.2f", workout.distance),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF32D74B)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "km",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${workout.duration} min • ${workout.calories} kcal",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

private fun getWorkoutEmoji(type: String): String {
    return when (type) {
        "Outdoor Walk" -> "🚶"
        "Running" -> "🏃"
        "Cycling" -> "🚴"
        "Hiking" -> "⛰️"
        else -> "🏃"
    }
}

private fun formatDate(timestamp: Long): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = timestamp

    val now = java.util.Calendar.getInstance()
    val isToday = calendar.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR) &&
            calendar.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)

    return if (isToday) {
        String.format("Dzisiaj, %02d:%02d",
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE))
    } else {
        String.format("%02d.%02d.%d, %02d:%02d",
            calendar.get(java.util.Calendar.DAY_OF_MONTH),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE))
    }
}