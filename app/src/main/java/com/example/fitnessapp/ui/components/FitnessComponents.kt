package com.example.fitnessapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * Przycisk nawigacji dolnej
 */
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