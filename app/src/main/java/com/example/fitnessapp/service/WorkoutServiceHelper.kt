package com.example.fitnessapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fitnessapp.MainActivity
import com.example.fitnessapp.R

/**
 * Helper do zarządzania stanem treningu i powiadomieniami
 *
 * Dodaj te metody do swojego WorkoutTrackingService lub użyj ich bezpośrednio
 */
object WorkoutServiceHelper {

    private const val CHANNEL_ID = "workout_tracking_channel"
    private const val NOTIFICATION_ID = 1001

    /**
     * Ustawia flagę aktywnego treningu
     */
    fun setWorkoutActive(context: Context, isActive: Boolean, workoutType: String = "") {
        val prefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("workout_active", isActive)
            .putString("current_workout_type", workoutType)
            .apply()
    }

    /**
     * Sprawdza czy trening jest aktywny
     */
    fun isWorkoutActive(context: Context): Boolean {
        val prefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("workout_active", false)
    }

    /**
     * Pobiera typ aktywnego treningu
     */
    fun getActiveWorkoutType(context: Context): String {
        val prefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        return prefs.getString("current_workout_type", "Workout") ?: "Workout"
    }

    /**
     * Tworzy kanał powiadomień (wymagane dla Android 8+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Śledzenie treningu",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Powiadomienia o aktywnym treningu"
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Tworzy powiadomienie treningu z możliwością powrotu do aplikacji
     */
    fun createWorkoutNotification(
        context: Context,
        workoutType: String,
        duration: String = "00:00",
        distance: String = "0.00 km"
    ): Notification {
        // Intent do otwarcia aplikacji na ekranie aktywnego treningu
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            Intent.setAction = MainActivity.ACTION_OPEN_ACTIVE_WORKOUT
            Intent.setFlags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("$workoutType w trakcie")
            .setContentText("$duration • $distance")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Zmień na własną ikonę
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .build()
    }

    /**
     * Aktualizuje powiadomienie treningu
     */
    fun updateNotification(
        context: Context,
        workoutType: String,
        duration: String,
        distance: String
    ) {
        val notification = createWorkoutNotification(context, workoutType, duration, distance)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

/**
 * Przykład użycia w WorkoutTrackingService:
 *
 * W metodzie startWorkout():
 *   WorkoutServiceHelper.setWorkoutActive(context, true, workoutType)
 *   WorkoutServiceHelper.createNotificationChannel(context)
 *   val notification = WorkoutServiceHelper.createWorkoutNotification(context, workoutType)
 *   startForeground(NOTIFICATION_ID, notification)
 *
 * W metodzie stopWorkout():
 *   WorkoutServiceHelper.setWorkoutActive(context, false)
 *   stopForeground(STOP_FOREGROUND_REMOVE)
 *
 * W pętli aktualizacji:
 *   WorkoutServiceHelper.updateNotification(context, workoutType, formattedDuration, formattedDistance)
 */