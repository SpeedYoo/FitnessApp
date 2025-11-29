package com.example.fitnessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Workout(
    val id: Int,
    val name: String,
    val icon: String,
    val backgroundColor: Color
)

@Composable
fun WorkoutScreen(
    onNavigateToSummary: () -> Unit,
    onStartWorkout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)

    var showOnlyFavorites by remember { mutableStateOf(false) }
    var favoriteWorkouts by remember {
        mutableStateOf(
            prefs.getStringSet("favorite_workouts", emptySet()) ?: emptySet()
        )
    }

    val workouts = listOf(
        Workout(1, "Outdoor Walk", "🚶", Color(0xFF2D4A1F)),
        Workout(2, "Running", "🏃", Color(0xFF2D4A1F)),
        Workout(3, "Cycling", "🚴", Color(0xFF2D4A1F)),
        Workout(4, "Hiking", "⛰️", Color(0xFF2D4A1F))
    )

    val filteredWorkouts = if (showOnlyFavorites) {
        workouts.filter { favoriteWorkouts.contains(it.name) }
    } else {
        workouts
    }

    val toggleFavorite: (String) -> Unit = { workoutName ->
        val newFavorites = favoriteWorkouts.toMutableSet()
        if (newFavorites.contains(workoutName)) {
            newFavorites.remove(workoutName)
        } else {
            newFavorites.add(workoutName)
        }
        favoriteWorkouts = newFavorites
        prefs.edit().putStringSet("favorite_workouts", newFavorites).apply()
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Workout",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            IconButton(
                onClick = { showOnlyFavorites = !showOnlyFavorites }
            ) {
                Icon(
                    imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (showOnlyFavorites) "Pokaż wszystkie" else "Pokaż ulubione",
                    tint = if (showOnlyFavorites) Color(0xFFFF3B30) else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista treningów
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredWorkouts.size) { index ->
                val workout = filteredWorkouts[index]
                WorkoutCard(
                    workout = workout,
                    isFavorite = favoriteWorkouts.contains(workout.name),
                    onStartWorkout = onStartWorkout,
                    onToggleFavorite = toggleFavorite
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dolne przyciski nawigacji
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNavigateToSummary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2E),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text("Statystyki")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF32D74B)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text("Treningi")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun WorkoutCard(
    workout: Workout,
    isFavorite: Boolean,
    onStartWorkout: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = workout.backgroundColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Ikona i nazwa treningu
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikona treningu
                Surface(
                    modifier = Modifier.size(50.dp),
                    color = Color.Transparent,
                    shape = CircleShape
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = workout.icon,
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = workout.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Przycisk play (góra-prawo)
            IconButton(
                onClick = { onStartWorkout(workout.name) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(50.dp)
                    .background(Color(0xFFB4FF00), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start ${workout.name}",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Ikona serca (prawo-dół)
            IconButton(
                onClick = { onToggleFavorite(workout.name) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle favorite",
                    tint = if (isFavorite) Color(0xFFFF3B30) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Dolne przyciski (lewy dół)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Przycisk timer/history
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFF1C3A14),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "⏱️", fontSize = 20.sp)
                    }
                }

                // Przycisk start workout
                Surface(
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 100.dp),
                    color = Color(0xFF1C3A14),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Start",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}