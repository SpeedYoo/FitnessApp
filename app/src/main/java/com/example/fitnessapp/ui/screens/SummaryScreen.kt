package com.example.fitnessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.ui.components.BottomNavigationBar
import com.example.fitnessapp.ui.components.BottomNavTab
import com.example.fitnessapp.ui.components.ProgressStatCard
import com.example.fitnessapp.ui.components.SimpleStatCard
import com.example.fitnessapp.ui.theme.*
import com.example.fitnessapp.ui.utils.FitnessUtils
import com.example.fitnessapp.ui.viewmodel.SummaryViewModel

@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    onNavigateToWorkout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Trigger do odświeżania licznika treningów
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        refreshTrigger++
        while (true) {
            kotlinx.coroutines.delay(2000)
            refreshTrigger++
        }
    }

    // Oblicz liczbę treningów w tym tygodniu
    val weeklyWorkouts by remember(refreshTrigger) {
        derivedStateOf {
            calculateWeeklyWorkouts(context)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundBlack,
        bottomBar = {
            BottomNavigationBar(
                selectedTab = BottomNavTab.SUMMARY,
                onNavigateToSummary = { },
                onNavigateToWorkout = onNavigateToWorkout
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.paddingLarge)
                .padding(top = Dimensions.paddingLarge)
        ) {
            // Nagłówek
            SummaryHeader(
                onNavigateToProfile = onNavigateToProfile
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

            // Karta Aktywności (Kalorie) - pełna szerokość z kółkiem
            val caloriesProgress = if (uiState.caloriesGoal > 0) {
                uiState.calories.toFloat() / uiState.caloriesGoal.toFloat()
            } else 0f

            ProgressStatCard(
                title = "Aktywność",
                subtitle = "Dzisiaj",
                currentValue = FitnessUtils.formatNumber(uiState.calories),
                goalValue = FitnessUtils.formatNumber(uiState.caloriesGoal),
                unit = "kcal",
                progress = caloriesProgress,
                progressColors = listOf(FitnessRed, FitnessRedLight)
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

            // Karta Kroków - pełna szerokość z kółkiem
            val stepsProgress = if (uiState.stepsGoal > 0) {
                uiState.steps.toFloat() / uiState.stepsGoal.toFloat()
            } else 0f

            ProgressStatCard(
                title = "Ilość kroków",
                subtitle = "Dzisiaj",
                currentValue = FitnessUtils.formatNumber(uiState.steps),
                goalValue = FitnessUtils.formatNumber(uiState.stepsGoal),
                unit = "kroków",
                progress = stepsProgress,
                progressColors = listOf(FitnessPurple, FitnessPurpleLight)
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

            // Rząd: Dystans, Treningi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                SimpleStatCard(
                    title = "Dystans",
                    subtitle = "Dzisiaj",
                    value = uiState.distance,
                    valueColor = FitnessIndigo,
                    modifier = Modifier.weight(1f)
                )

                SimpleStatCard(
                    title = "Treningi",
                    subtitle = "Ten tydzień",
                    value = "$weeklyWorkouts",
                    valueColor = FitnessYellow,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHistory
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            // Karta W ruchu - pełna szerokość
            SimpleStatCard(
                title = "W ruchu",
                subtitle = "Dzisiaj",
                value = "${uiState.activeTimeMinutes} minut",
                valueColor = FitnessGreen,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SummaryHeader(
    onNavigateToProfile: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Statystyki",
                style = FitnessTextStyles.screenTitle,
                color = TextWhite
            )
            Text(
                text = FitnessUtils.getCurrentDate(),
                style = FitnessTextStyles.dateText,
                color = TextGray
            )
        }

        IconButton(onClick = onNavigateToProfile) {
            Surface(
                modifier = Modifier.size(Dimensions.avatarSizeMedium),
                color = SurfaceDark,
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
}

/**
 * Oblicza liczbę treningów w bieżącym tygodniu
 */
private fun calculateWeeklyWorkouts(context: android.content.Context): Int {
    val prefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)
    val workoutCount = prefs.getInt("workout_count", 0)

    var weekCount = 0
    for (i in 1..workoutCount) {
        val isDeleted = prefs.getBoolean("workout_${i}_deleted", false)
        if (isDeleted) continue

        val timestamp = prefs.getLong("workout_${i}_timestamp", 0)
        if (timestamp == 0L) continue

        if (FitnessUtils.isThisWeek(timestamp)) {
            weekCount++
        }
    }
    return weekCount
}