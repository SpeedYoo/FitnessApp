package com.example.fitnessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.ui.viewmodel.SummaryViewModel

@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    onNavigateToWorkout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Obserwowanie stanu z ViewModela
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Trigger do odświeżania licznika treningów
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Odśwież gdy ekran się pokazuje
    LaunchedEffect(Unit) {
        refreshTrigger++
    }

    // Oblicz liczbę treningów w tym tygodniu - z odświeżaniem
    val weeklyWorkouts by remember(refreshTrigger) {
        derivedStateOf {
            val prefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)
            val workoutCount = prefs.getInt("workout_count", 0)
            val calendar = java.util.Calendar.getInstance()
            val currentWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
            val currentYear = calendar.get(java.util.Calendar.YEAR)

            var weekCount = 0
            for (i in 1..workoutCount) {
                // WAŻNE: Sprawdź czy nie został usunięty
                val isDeleted = prefs.getBoolean("workout_${i}_deleted", false)
                if (isDeleted) continue

                val timestamp = prefs.getLong("workout_${i}_timestamp", 0)
                if (timestamp == 0L) continue

                calendar.timeInMillis = timestamp
                val workoutWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
                val workoutYear = calendar.get(java.util.Calendar.YEAR)

                if (workoutWeek == currentWeek && workoutYear == currentYear) {
                    weekCount++
                }
            }
            weekCount
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        // Nagłówek z ikoną profilu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Statystyki",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = getCurrentDate(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onNavigateToProfile) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFF2C2C2E),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "👤",
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Karta aktywności (główna) - DANE Z VIEWMODELA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2E)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular progress indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    val progress = if (uiState.caloriesGoal > 0) {
                        uiState.calories.toFloat() / uiState.caloriesGoal.toFloat()
                    } else {
                        0f
                    }

                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFFF3B30),
                        strokeWidth = 8.dp,
                        trackColor = Color(0xFF3A3A3C),
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = "Aktywność",
                        fontSize = 16.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "${uiState.calories}/${uiState.caloriesGoal} kcal",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF3B30)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rząd z kartami: Ilość kroków i Dystans - DANE Z VIEWMODELA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Ilość kroków",
                subtitle = "Dzisiaj",
                value = "${formatNumber(uiState.steps)} / ${formatNumber(uiState.stepsGoal)}",
                valueColor = Color(0xFFBF5AF2),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Dystans",
                subtitle = "Dzisiaj",
                value = uiState.distance,
                valueColor = Color(0xFF5E5CE6),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rząd z kartami: Treningi i W ruchu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Karta treningów - KLIKALNA!
            Card(
                onClick = onNavigateToHistory,
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2C2C2E)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Treningi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = "W tym tygodniu",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }

                    Text(
                        text = "$weeklyWorkouts",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFCC00)
                    )
                }
            }

            StatCard(
                title = "W ruchu",
                subtitle = "Dzisiaj",
                value = "${uiState.activeTimeMinutes} minut",
                valueColor = Color(0xFF32D74B),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Dolne przyciski nawigacji
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF32D74B)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text("Statystyki")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onNavigateToWorkout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2E),
                    contentColor = Color.White
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
fun StatCard(
    title: String,
    subtitle: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2E)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

// Helper do formatowania liczb (10000 -> "10 000")
private fun formatNumber(number: Int): String {
    return number.toString()
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()
}

// Helper do wyświetlania dzisiejszej daty
private fun getCurrentDate(): String {
    val calendar = java.util.Calendar.getInstance()
    val dayNames = arrayOf("Niedziela", "Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota")
    val monthNames = arrayOf("stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca",
        "lipca", "sierpnia", "września", "października", "listopada", "grudnia")

    val dayOfWeek = dayNames[calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1]
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val month = monthNames[calendar.get(java.util.Calendar.MONTH)]

    return "$dayOfWeek, $day $month"
}