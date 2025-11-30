package com.example.fitnessapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Schemat kolorów dla aplikacji fitness (zawsze ciemny motyw)
 */
private val FitnessDarkColorScheme = darkColorScheme(
    primary = FitnessGreen,
    onPrimary = Color.Black,
    secondary = FitnessPurple,
    onSecondary = Color.White,
    tertiary = FitnessRed,
    onTertiary = Color.White,
    background = BackgroundBlack,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceDarkSecondary,
    onSurfaceVariant = TextLightGray,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun FitnessAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FitnessDarkColorScheme,
        typography = Typography,
        content = content
    )
}