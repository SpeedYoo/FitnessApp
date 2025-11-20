package com.example.fitnessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier
) {
    val workouts = listOf(
        Workout(1, "Spacer", "🚶", Color(0xFF2D4A1F)),
        Workout(4, "Marsz", "🏃", Color(0xFF2D4A1F)),
        Workout(2, "Bieganie", "🏋️", Color(0xFF2D4A1F)),
        Workout(3, "Jazda na rowerze", "🚶", Color(0xFF2D4A1F)),
        Workout(5, "Ćwiczenia siłowe", "🚴", Color(0xFF2D4A1F)),
        Workout(6, "Joga", "🧘", Color(0xFF2D4A1F))
    )

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

            Row {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start workout",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorites",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista treningów
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(workouts) { workout ->
                WorkoutCard(workout = workout)
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
fun WorkoutCard(workout: Workout) {
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
                onClick = { },
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
                onClick = { },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Add to favorites",
                    tint = Color.White.copy(alpha = 0.6f),
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