package com.example.fitnessapp.domain.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.fitnessapp.MainActivity
import com.google.android.gms.location.*
import androidx.core.content.edit

/**
 * Foreground Service do śledzenia treningu z GPS
 */
class WorkoutTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var workoutStartTime = 0L
    private var pausedTime = 0L // Czas spędzony na pauzie
    private var pauseStartTime = 0L
    private var isTracking = false
    private var isPaused = false
    private var totalDistance = 0f // w metrach
    private var lastLocation: Location? = null
    private val routePoints = mutableListOf<RoutePoint>()

    private var workoutType = "Outdoor Walk"

    // Timer do odświeżania UI
    private var updateTimer: java.util.Timer? = null

    companion object {
        const val CHANNEL_ID = "WorkoutTrackingChannel"
        const val NOTIFICATION_ID = 2

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"

        const val EXTRA_WORKOUT_TYPE = "workout_type"

        const val ACTION_WORKOUT_UPDATE = "com.example.fitnessapp.WORKOUT_UPDATE"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_DISTANCE = "distance"
        const val EXTRA_CALORIES = "calories"

        fun startWorkout(context: Context, workoutType: String) {

            if (!hasLocationPermission(context)) {
                android.widget.Toast.makeText(
                    context,
                    "Brak uprawnień lokalizacji. Przyznaj uprawnienia w ustawieniach.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return
            }

            val intent = Intent(context, WorkoutTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_WORKOUT_TYPE, workoutType)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        private fun hasLocationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

        fun pauseWorkout(context: Context) {
            val intent = Intent(context, WorkoutTrackingService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeWorkout(context: Context) {
            val intent = Intent(context, WorkoutTrackingService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stopWorkout(context: Context) {
            val intent = Intent(context, WorkoutTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(0, 0f))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (!isTracking || isPaused) return

                locationResult.lastLocation?.let { location ->
                    handleNewLocation(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                workoutType = intent.getStringExtra(EXTRA_WORKOUT_TYPE) ?: "Outdoor Walk"
                // Zapisz typ treningu do SharedPreferences
                val prefs = getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
                prefs.edit { putString("current_workout_type", workoutType) }
                startTracking()
            }
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        isTracking = true
        isPaused = false
        workoutStartTime = System.currentTimeMillis()
        pausedTime = 0L
        totalDistance = 0f
        lastLocation = null
        routePoints.clear()

        // Uruchom timer do odświeżania UI co sekundę
        updateTimer = java.util.Timer()
        updateTimer?.schedule(object : java.util.TimerTask() {
            override fun run() {
                if (isTracking && !isPaused) {
                    updateNotification()
                    broadcastUpdate()
                }
            }
        }, 0, 1000) // Co 1 sekundę

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000 // Co 3 sekundy
        ).apply {
            setMinUpdateIntervalMillis(1000) // Min co 1 sekundę
            setMaxUpdateDelayMillis(5000)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun pauseTracking() {
        isPaused = true
        pauseStartTime = System.currentTimeMillis()
        updateNotification()
    }

    private fun resumeTracking() {
        if (isPaused) {
            pausedTime += System.currentTimeMillis() - pauseStartTime
            isPaused = false
        }
        updateNotification()
    }

    private fun stopTracking() {
        // WAŻNE: Najpierw zapisz trening (gdy isTracking=true), potem zatrzymaj
        android.util.Log.d("WorkoutService", "stopTracking() called")

        fusedLocationClient.removeLocationUpdates(locationCallback)
        updateTimer?.cancel()
        updateTimer = null

        // Zapisz trening PRZED zmianą isTracking na false!
        saveWorkout()

        // Dopiero teraz przestań trackować
        isTracking = false

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleNewLocation(location: Location) {
        android.util.Log.d("WorkoutService", "New GPS location: lat=${location.latitude}, lon=${location.longitude}, accuracy=${location.accuracy}m")

        // Dodaj punkt do trasy
        routePoints.add(RoutePoint(
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = System.currentTimeMillis()
        ))

        android.util.Log.d("WorkoutService", "Total route points: ${routePoints.size}")

        // Oblicz dystans
        lastLocation?.let { last ->
            val distance = last.distanceTo(location)
            android.util.Log.d("WorkoutService", "Distance from last point: ${distance}m")
            // Ignoruj skoki GPS (>50m w 3 sekundy to ~60km/h)
            if (distance < 50) {
                totalDistance += distance
                android.util.Log.d("WorkoutService", "Total distance: ${totalDistance}m (${totalDistance/1000}km)")
            } else {
                android.util.Log.d("WorkoutService", "GPS jump detected, ignoring (${distance}m)")
            }
        }
        lastLocation = location

        updateNotification()
        broadcastUpdate()
    }

    private fun getDuration(): Long {
        val elapsed = if (isTracking) {
            System.currentTimeMillis() - workoutStartTime - pausedTime
        } else {
            // Jeśli już nie trackujemy, użyj ostatniego znanego czasu
            if (workoutStartTime > 0) {
                val endTime = System.currentTimeMillis()
                endTime - workoutStartTime - pausedTime
            } else {
                0
            }
        }

        android.util.Log.d("WorkoutService", "getDuration() called:")
        android.util.Log.d("WorkoutService", "  isTracking: $isTracking")
        android.util.Log.d("WorkoutService", "  workoutStartTime: $workoutStartTime")
        android.util.Log.d("WorkoutService", "  currentTime: ${System.currentTimeMillis()}")
        android.util.Log.d("WorkoutService", "  pausedTime: $pausedTime")
        android.util.Log.d("WorkoutService", "  elapsed: $elapsed")

        return elapsed.coerceAtLeast(0)
    }

    private fun getCalories(): Int {
        val prefs = getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        val weight = prefs.getFloat("weight", 70f)

        val distanceKm = totalDistance / 1000
        val durationMinutes = getDuration() / 1000 / 60

        // MET dla różnych aktywności
        val met = when (workoutType) {
            "Running" -> 8.0f
            "Cycling" -> 6.0f
            "Indoor Walk" -> 3.5f
            else -> 4.0f // Outdoor Walk
        }

        val timeInHours = durationMinutes / 60f
        return (met * weight * timeInHours).toInt()
    }

    private fun updateNotification() {
        val notification = createNotification(
            getDuration() / 1000,
            totalDistance / 1000
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun broadcastUpdate() {
        val intent = Intent(ACTION_WORKOUT_UPDATE).apply {
            putExtra(EXTRA_DURATION, getDuration())
            putExtra(EXTRA_DISTANCE, totalDistance / 1000)
            putExtra(EXTRA_CALORIES, getCalories())
        }
        sendBroadcast(intent)
    }

    @SuppressLint("DefaultLocale")
    private fun createNotification(durationSeconds: Long, distanceKm: Float): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            action = "OPEN_ACTIVE_WORKOUT"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val status = if (isPaused) "⏸ Wstrzymano" else "🏃 Trening"
        val time = formatDuration(durationSeconds)
        val distance = String.format("%.2f km", distanceKm)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$status - $workoutType")
            .setContentText("$time • $distance")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Śledzenie treningów",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Wyświetla postęp treningu"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    private fun saveWorkout() {
        val prefs = getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)

        val durationMs = getDuration()
        val durationSeconds = durationMs / 1000
        val durationMinutes = durationSeconds / 60

        android.util.Log.d("WorkoutService", "Saving workout:")
        android.util.Log.d("WorkoutService", "Duration MS: $durationMs")
        android.util.Log.d("WorkoutService", "Duration Seconds: $durationSeconds")
        android.util.Log.d("WorkoutService", "Duration Minutes: $durationMinutes")
        android.util.Log.d("WorkoutService", "Distance: ${totalDistance / 1000} km")
        android.util.Log.d("WorkoutService", "Calories: ${getCalories()}")

        // Zapisz podstawowe dane
        prefs.edit().apply {
            putLong("last_workout_duration", durationMinutes)
            putFloat("last_workout_distance", totalDistance / 1000)
            putInt("last_workout_calories", getCalories())
            putString("last_workout_type", workoutType)
            putLong("last_workout_timestamp", System.currentTimeMillis())
            apply()
        }

        // Zapisz do historii
        saveWorkoutToHistory(durationMinutes)
    }

    private fun saveWorkoutToHistory(durationMinutes: Long) {
        val prefs = getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)

        // Pobierz obecną liczbę treningów
        val workoutCount = prefs.getInt("workout_count", 0)
        val newCount = workoutCount + 1

        // Zapisz punkty trasy jako JSON (jeśli są)
        val routeJson = if (routePoints.isNotEmpty()) {
            try {
                com.google.gson.Gson().toJson(routePoints)
            } catch (e: Exception) {
                "[]"
            }
        } else {
            "[]"
        }

        android.util.Log.d("WorkoutService", "Saving workout $newCount with ${routePoints.size} GPS points")

        prefs.edit().apply {
            putInt("workout_count", newCount)
            putString("workout_${newCount}_type", workoutType)
            putLong("workout_${newCount}_timestamp", System.currentTimeMillis())
            putLong("workout_${newCount}_duration", durationMinutes)
            putFloat("workout_${newCount}_distance", totalDistance / 1000)
            putInt("workout_${newCount}_calories", getCalories())
            putString("workout_${newCount}_route", routeJson)
            putBoolean("workout_${newCount}_deleted", false)
            apply()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        updateTimer?.cancel()
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}

data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)