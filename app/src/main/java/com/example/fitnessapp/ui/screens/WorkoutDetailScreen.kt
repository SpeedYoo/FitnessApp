package com.example.fitnessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    val prefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)

    // Załaduj dane treningu
    val workoutType = prefs.getString("workout_${workoutId}_type", "Unknown") ?: "Unknown"
    val timestamp = prefs.getLong("workout_${workoutId}_timestamp", 0)
    val duration = prefs.getLong("workout_${workoutId}_duration", 0)
    val distance = prefs.getFloat("workout_${workoutId}_distance", 0f)
    val calories = prefs.getInt("workout_${workoutId}_calories", 0)
    val routeJson = prefs.getString("workout_${workoutId}_route", "[]") ?: "[]"

    android.util.Log.d("WorkoutDetail", "Loading workout $workoutId")
    android.util.Log.d("WorkoutDetail", "Type: $workoutType")
    android.util.Log.d("WorkoutDetail", "Timestamp: $timestamp")
    android.util.Log.d("WorkoutDetail", "Duration: $duration min")
    android.util.Log.d("WorkoutDetail", "Distance: $distance km")
    android.util.Log.d("WorkoutDetail", "Calories: $calories")
    android.util.Log.d("WorkoutDetail", "Route JSON length: ${routeJson.length}")

    // Parsuj trasę GPS
    val routePoints = remember {
        try {
            val type = object : TypeToken<List<RoutePointData>>() {}.type
            Gson().fromJson<List<RoutePointData>>(routeJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // Nagłówek
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Powrót",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = workoutType,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = formatDetailedDate(timestamp),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń",
                    tint = Color(0xFFFF3B30),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Mapa GPS lub placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2C2C2E)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (routePoints.isNotEmpty()) {
                    // TODO: Mapa GPS - wymaga Google Maps API Key
                    // Na razie pokazujemy info o trasie
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗺️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Trasa GPS zapisana",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${routePoints.size} punktów GPS",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗺️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Brak trasy GPS",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Statystyki
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailStatCard(
                    label = "Dystans",
                    value = String.format("%.2f", distance),
                    unit = "km",
                    color = Color(0xFF32D74B)
                )

                DetailStatCard(
                    label = "Czas",
                    value = duration.toString(),
                    unit = "min",
                    color = Color(0xFF5E5CE6)
                )

                DetailStatCard(
                    label = "Kalorie",
                    value = calories.toString(),
                    unit = "kcal",
                    color = Color(0xFFFF3B30)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dodatkowe statystyki
            if (distance > 0 && duration > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2C2C2E)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Statystyki",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        StatRow("Tempo", String.format("%.2f min/km", duration / distance))
                        StatRow("Średnia prędkość", String.format("%.2f km/h", distance / (duration / 60f)))
                        if (routePoints.isNotEmpty()) {
                            StatRow("Punkty GPS", "${routePoints.size}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)            )
        }
    }

    // Dialog usuwania
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń trening?") },
            text = { Text("Czy na pewno chcesz usunąć ten trening? Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Usuń trening
                        deleteWorkout(context, workoutId)
                        onNavigateBack()
                    }
                ) {
                    Text("Usuń", color = Color(0xFFFF3B30))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

// MapViewComposable zakomentowane - wymaga Google Maps API Key
// Odkomentuj gdy dodasz klucz API do AndroidManifest.xml
/*
@Composable
fun MapViewComposable(routePoints: List<RoutePointData>) {
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                onCreate(null)
                getMapAsync { googleMap ->
                    if (routePoints.isNotEmpty()) {
                        val polylineOptions = PolylineOptions()
                            .color(android.graphics.Color.rgb(50, 215, 75))
                            .width(10f)

                        routePoints.forEach { point ->
                            polylineOptions.add(LatLng(point.latitude, point.longitude))
                        }

                        googleMap.addPolyline(polylineOptions)

                        val boundsBuilder = LatLngBounds.Builder()
                        routePoints.forEach { point ->
                            boundsBuilder.include(LatLng(point.latitude, point.longitude))
                        }

                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngBounds(
                                boundsBuilder.build(),
                                100
                            )
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
*/
@Composable
fun DetailStatCard(
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Card(
        modifier = Modifier.size(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2E)
        ),
        shape = RoundedCornerShape(12.dp)
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
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = unit,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.LightGray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

private fun formatDetailedDate(timestamp: Long): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = timestamp

    return String.format(
        "%02d.%02d.%d, %02d:%02d",
        calendar.get(java.util.Calendar.DAY_OF_MONTH),
        calendar.get(java.util.Calendar.MONTH) + 1,
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE)
    )
}

private fun deleteWorkout(context: android.content.Context, workoutId: Int) {
    val prefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)
    // Zamiast usuwać dane, oznacz jako usunięty
    prefs.edit().putBoolean("workout_${workoutId}_deleted", true).apply()

    android.util.Log.d("WorkoutDetail", "Marked workout $workoutId as deleted")
}