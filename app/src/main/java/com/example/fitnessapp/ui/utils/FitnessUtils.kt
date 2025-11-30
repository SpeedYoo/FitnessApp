package com.example.fitnessapp.ui.utils

import java.util.Calendar

/**
 * Pomocnicze funkcje dla aplikacji fitness
 */
object FitnessUtils {

    /**
     * Formatuje liczbę z separatorami tysięcy (10000 -> "10 000")
     */
    fun formatNumber(number: Int): String {
        return number.toString()
            .reversed()
            .chunked(3)
            .joinToString(" ")
            .reversed()
    }

    /**
     * Formatuje liczbę zmiennoprzecinkową z określoną precyzją
     */
    fun formatDecimal(number: Float, decimals: Int = 2): String {
        return String.format("%.${decimals}f", number)
    }

    /**
     * Zwraca aktualną datę w formacie "Poniedziałek, 15 stycznia"
     */
    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dayNames = arrayOf(
            "Niedziela", "Poniedziałek", "Wtorek", "Środa",
            "Czwartek", "Piątek", "Sobota"
        )
        val monthNames = arrayOf(
            "stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca",
            "lipca", "sierpnia", "września", "października", "listopada", "grudnia"
        )

        val dayOfWeek = dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = monthNames[calendar.get(Calendar.MONTH)]

        return "$dayOfWeek, $day $month"
    }

    /**
     * Formatuje timestamp na szczegółową datę "15.01.2024, 14:30"
     */
    fun formatDetailedDate(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return String.format(
            "%02d.%02d.%d, %02d:%02d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
    }

    /**
     * Formatuje czas trwania (w minutach) na czytelny format
     */
    fun formatDuration(minutes: Long): String {
        return when {
            minutes < 60 -> "$minutes min"
            minutes < 1440 -> {
                val hours = minutes / 60
                val mins = minutes % 60
                if (mins > 0) "${hours}h ${mins}min" else "${hours}h"
            }
            else -> {
                val days = minutes / 1440
                val hours = (minutes % 1440) / 60
                if (hours > 0) "${days}d ${hours}h" else "${days}d"
            }
        }
    }

    /**
     * Oblicza tempo (min/km) na podstawie dystansu i czasu
     */
    fun calculatePace(distanceKm: Float, durationMinutes: Long): String {
        if (distanceKm <= 0) return "--"
        val pace = durationMinutes / distanceKm
        return String.format("%.2f min/km", pace)
    }

    /**
     * Oblicza średnią prędkość (km/h) na podstawie dystansu i czasu
     */
    fun calculateSpeed(distanceKm: Float, durationMinutes: Long): String {
        if (durationMinutes <= 0) return "--"
        val speed = distanceKm / (durationMinutes / 60f)
        return String.format("%.2f km/h", speed)
    }

    /**
     * Sprawdza czy timestamp jest z tego tygodnia
     */
    fun isThisWeek(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        val timestampWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val timestampYear = calendar.get(Calendar.YEAR)

        return currentWeek == timestampWeek && currentYear == timestampYear
    }

    /**
     * Sprawdza czy timestamp jest z dzisiaj
     */
    fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = Triple(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        calendar.timeInMillis = timestamp
        val timestampDate = Triple(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        return today == timestampDate
    }

    /**
     * Formatuje datę z opcją "Dzisiaj"
     */
    fun formatDateWithToday(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return if (isToday(timestamp)) {
            String.format(
                "Dzisiaj, %02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
        } else {
            formatDetailedDate(timestamp)
        }
    }

    /**
     * Formatuje czas trwania z sekund na "HH:MM:SS" lub "MM:SS"
     */
    fun formatDurationFromSeconds(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    /**
     * Formatuje czas trwania z minut na "Xh XXm" lub "X min"
     */
    fun formatDurationFromMinutes(minutes: Long): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) {
            String.format("%dh %02dm", hours, mins)
        } else {
            String.format("%d min", mins)
        }
    }

    /**
     * Zwraca emoji dla typu treningu
     */
    fun getWorkoutEmoji(type: String): String {
        return when (type.lowercase()) {
            "spacer", "outdoor walk", "walk" -> "🚶"
            "bieganie", "running", "run" -> "🏃"
            "jazda na rowerze", "cycling", "bike" -> "🚴"
            "chodzenie po górach", "hiking", "hike" -> "⛰️"
            else -> "🏃"
        }
    }
}