package com.example.fitnessapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.ui.components.EmptyState
import com.example.fitnessapp.ui.components.ScreenHeader
import com.example.fitnessapp.ui.theme.*
import com.example.fitnessapp.ui.utils.FitnessUtils

/**
 * Model danych dla historii treningu
 */
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

    // Stan odświeżania
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        refreshTrigger++
    }

    // Załaduj historię treningów
    val workouts by remember(refreshTrigger) {
        derivedStateOf {
            loadWorkoutHistory(context)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .padding(Dimensions.paddingLarge)
    ) {
        // Nagłówek
        ScreenHeader(
            title = "Historia treningów",
            onNavigateBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

        if (workouts.isEmpty()) {
            EmptyState(
                emoji = "🏃",
                title = "Brak treningów",
                subtitle = "Rozpocznij swój pierwszy trening!"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                items(workouts) { workout ->
                    WorkoutHistoryCard(
                        workout = workout,
                        onClick = { onWorkoutClick(workout.id) }
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
            .height(Dimensions.workoutHistoryCardHeight),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(Dimensions.cornerRadiusMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lewa strona - typ i data
            WorkoutHistoryCardLeft(workout = workout)

            // Prawa strona - statystyki
            WorkoutHistoryCardRight(workout = workout)
        }
    }
}

@Composable
private fun WorkoutHistoryCardLeft(workout: WorkoutHistoryItem) {
    Column {
        Text(
            text = FitnessUtils.getWorkoutEmoji(workout.type),
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
        Text(
            text = workout.type,
            style = FitnessTextStyles.cardTitle,
            color = TextWhite
        )
        Text(
            text = FitnessUtils.formatDateWithToday(workout.timestamp),
            style = FitnessTextStyles.cardSubtitle,
            color = TextGray
        )
    }
}

@Composable
private fun WorkoutHistoryCardRight(workout: WorkoutHistoryItem) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = FitnessUtils.formatDecimal(workout.distance),
                style = FitnessTextStyles.statisticValue,
                color = FitnessGreen
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "km",
                style = FitnessTextStyles.dateText,
                color = TextGray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${FitnessUtils.formatDurationFromMinutes(workout.duration)} • ${workout.calories} kcal",
            style = FitnessTextStyles.cardSubtitle,
            color = TextLightGray
        )
    }
}

/**
 * Ładuje historię treningów z SharedPreferences
 */
private fun loadWorkoutHistory(context: Context): List<WorkoutHistoryItem> {
    val prefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
    val workoutCount = prefs.getInt("workout_count", 0)
    val list = mutableListOf<WorkoutHistoryItem>()

    android.util.Log.d("WorkoutHistory", "Total workouts in database: $workoutCount")

    for (i in workoutCount downTo 1) {
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
    return list
}