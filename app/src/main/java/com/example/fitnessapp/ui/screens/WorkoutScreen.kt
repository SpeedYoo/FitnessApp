package com.example.fitnessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.fitnessapp.ui.components.BottomNavigationBar
import com.example.fitnessapp.ui.components.BottomNavTab
import com.example.fitnessapp.ui.theme.*

/**
 * Model danych dla treningu
 */
data class Workout(
    val id: Int,
    val name: String,
    val icon: String,
    val backgroundColor: Color = WorkoutCardGreenDark
)

/**
 * Lista dostępnych treningów
 */
val availableWorkouts = listOf(
    Workout(1, "Spacer", "🚶"),
    Workout(2, "Bieganie", "🏃"),
    Workout(3, "Jazda na rowerze", "🚴"),
    Workout(4, "Chodzenie po górach", "⛰️")
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

    val filteredWorkouts = if (showOnlyFavorites) {
        availableWorkouts.filter { favoriteWorkouts.contains(it.name) }
    } else {
        availableWorkouts
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundBlack,
        bottomBar = {
            BottomNavigationBar(
                selectedTab = BottomNavTab.WORKOUT,
                onNavigateToSummary = onNavigateToSummary,
                onNavigateToWorkout = { }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimensions.paddingLarge)
                .padding(top = Dimensions.paddingLarge)
        ) {
            // Nagłówek
            WorkoutScreenHeader(
                showOnlyFavorites = showOnlyFavorites,
                onToggleFavorites = { showOnlyFavorites = !showOnlyFavorites }
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

            // Lista treningów
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredWorkouts) { workout ->
                    WorkoutCard(
                        workout = workout,
                        isFavorite = favoriteWorkouts.contains(workout.name),
                        onStartWorkout = onStartWorkout,
                        onToggleFavorite = toggleFavorite
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutScreenHeader(
    showOnlyFavorites: Boolean,
    onToggleFavorites: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Workout",
            style = FitnessTextStyles.screenTitle,
            color = TextWhite
        )

        IconButton(onClick = onToggleFavorites) {
            Icon(
                imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (showOnlyFavorites) "Pokaż wszystkie" else "Pokaż ulubione",
                tint = if (showOnlyFavorites) FitnessRed else TextWhite,
                modifier = Modifier.size(Dimensions.iconSizeLarge)
            )
        }
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
            .height(Dimensions.workoutCardHeight),
        colors = CardDefaults.cardColors(containerColor = workout.backgroundColor),
        shape = RoundedCornerShape(Dimensions.cornerRadiusLarge)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingXLarge)
        ) {
            // Ikona treningu (góra-lewo)
            Text(
                text = workout.icon,
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Przycisk play (góra-prawo)
            PlayButton(
                onClick = { onStartWorkout(workout.name) },
                modifier = Modifier.align(Alignment.TopEnd)
            )

            // Nazwa treningu (dół-lewo)
            Text(
                text = workout.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.align(Alignment.BottomStart)
            )

            // Ikona serca (prawo-dół)
            FavoriteButton(
                isFavorite = isFavorite,
                onClick = { onToggleFavorite(workout.name) },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(50.dp)
            .background(WorkoutAccentLime, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Start",
            tint = Color.Black,
            modifier = Modifier.size(Dimensions.iconSizeLarge)
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(Dimensions.avatarSizeMedium)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Toggle favorite",
            tint = if (isFavorite) FitnessRed else TextWhite.copy(alpha = 0.6f),
            modifier = Modifier.size(Dimensions.iconSizeMedium)
        )
    }
}