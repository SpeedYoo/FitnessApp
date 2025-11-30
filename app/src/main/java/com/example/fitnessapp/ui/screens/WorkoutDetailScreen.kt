package com.example.fitnessapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.ui.components.ScreenHeaderWithSubtitle
import com.example.fitnessapp.ui.components.StatRow
import com.example.fitnessapp.ui.components.WorkoutStatCard
import com.example.fitnessapp.ui.theme.*
import com.example.fitnessapp.ui.utils.FitnessUtils
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.compose.*

/**
 * Model danych dla punktu trasy GPS
 */
data class RoutePointData(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

@Composable
fun WorkoutDetailScreen(
    workoutId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Załaduj dane treningu
    val workoutData = remember { loadWorkoutData(context, workoutId) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        // Nagłówek
        Box(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            ScreenHeaderWithSubtitle(
                title = workoutData.type,
                subtitle = FitnessUtils.formatDetailedDate(workoutData.timestamp),
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń",
                            tint = FitnessRed,
                            modifier = Modifier.size(Dimensions.iconSizeMedium)
                        )
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Mapa GPS
            WorkoutMapCard(routePoints = workoutData.routePoints)

            Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

            // Główne statystyki
            WorkoutMainStats(workoutData = workoutData)

            Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

            // Dodatkowe statystyki
            if (workoutData.distance > 0 && workoutData.duration > 0) {
                WorkoutDetailedStats(workoutData = workoutData)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Dialog usuwania
    if (showDeleteDialog) {
        DeleteWorkoutDialog(
            onConfirm = {
                deleteWorkout(context, workoutId)
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun WorkoutMapCard(routePoints: List<RoutePointData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = Dimensions.paddingLarge),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(Dimensions.cornerRadiusMedium)
    ) {
        if (routePoints.isNotEmpty()) {
            WorkoutMapView(routePoints = routePoints)
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🗺️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                    Text(
                        "Brak trasy GPS",
                        style = FitnessTextStyles.cardTitle,
                        color = TextGray
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutMapView(routePoints: List<RoutePointData>) {
    val latLngPoints = remember(routePoints) {
        routePoints.map { LatLng(it.latitude, it.longitude) }
    }

    val bounds = remember(latLngPoints) {
        if (latLngPoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            latLngPoints.forEach { boundsBuilder.include(it) }
            boundsBuilder.build()
        } else null
    }

    val cameraPositionState = rememberCameraPositionState {
        if (latLngPoints.isNotEmpty()) {
            position = CameraPosition.fromLatLngZoom(latLngPoints.first(), 15f)
        }
    }

    LaunchedEffect(bounds) {
        bounds?.let {
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(it, 50),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    ) {
        if (latLngPoints.size >= 2) {
            Polyline(
                points = latLngPoints,
                color = FitnessGreen,
                width = 12f
            )
        }

        if (latLngPoints.isNotEmpty()) {
            Marker(
                state = MarkerState(position = latLngPoints.first()),
                title = "Start",
                snippet = "Początek treningu"
            )
        }

        if (latLngPoints.size > 1) {
            Marker(
                state = MarkerState(position = latLngPoints.last()),
                title = "Koniec",
                snippet = "Koniec treningu"
            )
        }
    }
}

@Composable
private fun WorkoutMainStats(workoutData: WorkoutData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingLarge),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WorkoutStatCard(
            label = "Dystans",
            value = FitnessUtils.formatDecimal(workoutData.distance),
            unit = "km",
            valueColor = FitnessGreen
        )

        WorkoutStatCard(
            label = "Czas",
            value = workoutData.duration.toString(),
            unit = "min",
            valueColor = FitnessIndigo
        )

        WorkoutStatCard(
            label = "Kalorie",
            value = workoutData.calories.toString(),
            unit = "kcal",
            valueColor = FitnessRed
        )
    }
}

@Composable
private fun WorkoutDetailedStats(workoutData: WorkoutData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingLarge),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(Dimensions.cornerRadiusMedium)
    ) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            Text(
                "Statystyki",
                style = FitnessTextStyles.cardTitle,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

            StatRow(
                label = "Tempo",
                value = FitnessUtils.calculatePace(workoutData.distance, workoutData.duration)
            )
            StatRow(
                label = "Średnia prędkość",
                value = FitnessUtils.calculateSpeed(workoutData.distance, workoutData.duration)
            )
        }
    }
}

@Composable
private fun DeleteWorkoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Usuń trening?") },
        text = { Text("Czy na pewno chcesz usunąć ten trening? Tej operacji nie można cofnąć.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Usuń", color = FitnessRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

/**
 * Dane treningu
 */
private data class WorkoutData(
    val type: String,
    val timestamp: Long,
    val duration: Long,
    val distance: Float,
    val calories: Int,
    val routePoints: List<RoutePointData>
)

/**
 * Ładuje dane treningu z SharedPreferences
 */
private fun loadWorkoutData(context: Context, workoutId: Int): WorkoutData {
    val prefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)

    val type = prefs.getString("workout_${workoutId}_type", "Unknown") ?: "Unknown"
    val timestamp = prefs.getLong("workout_${workoutId}_timestamp", 0)
    val duration = prefs.getLong("workout_${workoutId}_duration", 0)
    val distance = prefs.getFloat("workout_${workoutId}_distance", 0f)
    val calories = prefs.getInt("workout_${workoutId}_calories", 0)
    val routeJson = prefs.getString("workout_${workoutId}_route", "[]") ?: "[]"

    android.util.Log.d("WorkoutDetail", "Loading workout $workoutId")
    android.util.Log.d("WorkoutDetail", "Type: $type, Duration: $duration min, Distance: $distance km")

    val routePoints = try {
        val listType = object : TypeToken<List<RoutePointData>>() {}.type
        Gson().fromJson<List<RoutePointData>>(routeJson, listType) ?: emptyList()
    } catch (e: Exception) {
        android.util.Log.e("WorkoutDetail", "Error parsing route: ${e.message}")
        emptyList()
    }

    return WorkoutData(type, timestamp, duration, distance, calories, routePoints)
}

/**
 * Oznacza trening jako usunięty
 */
private fun deleteWorkout(context: Context, workoutId: Int) {
    val prefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("workout_${workoutId}_deleted", true).apply()
    android.util.Log.d("WorkoutDetail", "Marked workout $workoutId as deleted")
}