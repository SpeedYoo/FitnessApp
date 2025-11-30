package com.example.fitnessapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animowane kółko postępu z gradientem i opcjonalną strzałką
 */
@Composable
fun AnimatedProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = Dimensions.progressIndicatorSizeLarge,
    strokeWidth: Dp = Dimensions.progressStrokeWidthLarge,
    gradientColors: List<Color> = listOf(FitnessRed, FitnessRedLight),
    trackColor: Color = SurfaceDarkSecondary,
    showArrow: Boolean = true,
    animationDuration: Int = 1000
) {
    // Animacja postępu
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "progress"
    )

    // Subtelna animacja pulsowania dla strzałki
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(modifier = modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        val radius = (size.toPx() - strokePx) / 2
        val center = Offset(size.toPx() / 2, size.toPx() / 2)

        // Tło (track)
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(strokePx / 2, strokePx / 2),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // Postęp z gradientem
        val sweepAngle = animatedProgress * 360f
        if (sweepAngle > 0) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = gradientColors + gradientColors.first(),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Strzałka na końcu postępu
        if (showArrow && animatedProgress > 0.02f) {
            val arrowAngle = -90f + sweepAngle
            val arrowRadians = Math.toRadians(arrowAngle.toDouble())

            val arrowX = center.x + radius * cos(arrowRadians).toFloat()
            val arrowY = center.y + radius * sin(arrowRadians).toFloat()

            // Mały trójkąt/strzałka
            val arrowSize = strokePx * 0.8f

            rotate(arrowAngle + 90f, pivot = Offset(arrowX, arrowY)) {
                // Rysuj trójkąt strzałki
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(arrowX, arrowY - arrowSize)
                    lineTo(arrowX - arrowSize * 0.6f, arrowY + arrowSize * 0.3f)
                    lineTo(arrowX + arrowSize * 0.6f, arrowY + arrowSize * 0.3f)
                    close()
                }
                drawPath(
                    path = path,
                    color = gradientColors.last().copy(alpha = pulseAlpha)
                )
            }
        }
    }
}

/**
 * Karta statystyk z kółkiem postępu
 */
@Composable
fun ProgressStatCard(
    title: String,
    subtitle: String,
    currentValue: String,
    goalValue: String,
    unit: String,
    progress: Float,
    progressColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardContent: @Composable ColumnScope.() -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingXLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kółko postępu
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Dimensions.progressIndicatorSizeLarge)
            ) {
                AnimatedProgressRing(
                    progress = progress,
                    gradientColors = progressColors,
                    showArrow = true
                )

                // Ikona w środku (opcjonalna)
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = FitnessTextStyles.cardTitle,
                    color = progressColors.first(),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(Dimensions.paddingXLarge))

            Column {
                Text(
                    text = title,
                    style = FitnessTextStyles.progressCardTitle,
                    color = TextLightGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = currentValue,
                        style = FitnessTextStyles.statisticValue,
                        color = progressColors.first()
                    )
                    Text(
                        text = "/$goalValue",
                        fontSize = 16.sp,
                        color = TextGray
                    )
                }
                Text(
                    text = unit,
                    style = FitnessTextStyles.cardSubtitle,
                    color = TextGray
                )
            }
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(Dimensions.cardHeightLarge),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(Dimensions.cornerRadiusLarge)
        ) {
            cardContent()
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(Dimensions.cardHeightLarge),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(Dimensions.cornerRadiusLarge)
        ) {
            cardContent()
        }
    }
}

/**
 * Prosta karta statystyk (bez kółka)
 */
@Composable
fun SimpleStatCard(
    title: String,
    subtitle: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardContent: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = FitnessTextStyles.cardTitle,
                    color = TextWhite
                )
                Text(
                    text = subtitle,
                    style = FitnessTextStyles.cardSubtitle,
                    color = TextWhite
                )
            }

            Text(
                text = value,
                style = FitnessTextStyles.statisticValue,
                color = valueColor
            )
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.height(Dimensions.cardHeightMedium),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(Dimensions.cornerRadiusLarge)
        ) {
            cardContent()
        }
    } else {
        Card(
            modifier = modifier.height(Dimensions.cardHeightMedium),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(Dimensions.cornerRadiusLarge)
        ) {
            cardContent()
        }
    }
}

enum class BottomNavTab {
    SUMMARY,
    WORKOUT
}

/**
 * Ujednolicony pasek dolnej nawigacji
 */
@Composable
fun BottomNavigationBar(
    selectedTab: BottomNavTab,
    onNavigateToSummary: () -> Unit,
    onNavigateToWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = BackgroundBlack
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.spacingLarge),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationButton(
                text = "Statystyki",
                isSelected = selectedTab == BottomNavTab.SUMMARY,
                onClick = onNavigateToSummary
            )

            Spacer(modifier = Modifier.width(Dimensions.spacingMedium))

            NavigationButton(
                text = "Treningi",
                isSelected = selectedTab == BottomNavTab.WORKOUT,
                onClick = onNavigateToWorkout
            )
        }
    }
}


@Composable
fun NavigationButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) SurfaceDarkSecondary else SurfaceDark,
            contentColor = if (isSelected) FitnessGreen else TextWhite
        ),
        shape = RoundedCornerShape(Dimensions.buttonCornerRadius),
        modifier = modifier.height(Dimensions.buttonHeight)
    ) {
        Text(text)
    }
}
@Composable
fun ScreenHeader(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Powrót",
                        tint = TextWhite,
                        modifier = Modifier.size(Dimensions.iconSizeLarge)
                    )
                }
                Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
            }
            Text(
                text = title,
                style = FitnessTextStyles.screenTitle,
                color = TextWhite
            )
        }
        Row {
            actions()
        }
    }
}

/**
 * Nagłówek ekranu z podtytułem
 */
@Composable
fun ScreenHeaderWithSubtitle(
    title: String,
    subtitle: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Powrót",
                        tint = TextWhite,
                        modifier = Modifier.size(Dimensions.iconSizeLarge)
                    )
                }
                Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
            }
            Column {
                Text(
                    text = title,
                    style = FitnessTextStyles.screenTitle,
                    color = TextWhite
                )
                Text(
                    text = subtitle,
                    style = FitnessTextStyles.dateText,
                    color = TextGray
                )
            }
        }
        Row {
            actions()
        }
    }
}

/**
 * Karta statystyk treningu (mała kwadratowa)
 */
@Composable
fun WorkoutStatCard(
    label: String,
    value: String,
    unit: String,
    valueColor: Color = TextWhite,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(Dimensions.workoutStatCardSize),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(Dimensions.cornerRadiusMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = FitnessTextStyles.cardSubtitle,
                color = TextLightGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = FitnessTextStyles.statisticValue,
                color = valueColor
            )
            Text(
                text = unit,
                fontSize = 10.sp,
                color = TextGray
            )
        }
    }
}

/**
 * Pole tekstowe dla formularzy profilu
 */
@Composable
fun FitnessTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    suffix: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = FitnessTextStyles.cardTitle,
            color = TextLightGray,
            modifier = Modifier.padding(bottom = Dimensions.spacingSmall)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextGray) },
            suffix = { Text(suffix, color = TextLightGray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = FitnessGreen,
                unfocusedBorderColor = SurfaceDarkSecondary,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                cursorColor = FitnessGreen
            ),
            shape = RoundedCornerShape(Dimensions.inputFieldCornerRadius),
            singleLine = true
        )
    }
}

/**
 * Główny przycisk akcji (zielony)
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimensions.buttonHeightLarge),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = FitnessGreen,
            contentColor = Color.Black,
            disabledContainerColor = SurfaceDarkSecondary,
            disabledContentColor = TextGray
        ),
        shape = RoundedCornerShape(Dimensions.inputFieldCornerRadius)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Karta informacyjna
 */
@Composable
fun InfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(Dimensions.inputFieldCornerRadius)
    ) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            Text(
                text = title,
                style = FitnessTextStyles.cardTitle,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
            Text(
                text = description,
                style = FitnessTextStyles.cardSubtitle,
                color = TextLightGray,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Pusty stan (empty state)
 */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 64.sp)
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = subtitle,
                style = FitnessTextStyles.dateText,
                color = TextGray
            )
        }
    }
}

/**
 * Wiersz ze statystyką (label - value)
 */
@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.spacingSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = FitnessTextStyles.dateText,
            color = TextLightGray
        )
        Text(
            text = value,
            style = FitnessTextStyles.dateText.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )
    }
}