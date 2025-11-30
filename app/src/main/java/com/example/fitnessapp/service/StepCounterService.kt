package com.example.fitnessapp.service

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.fitnessapp.MainActivity
import java.util.Calendar

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialSteps = -1
    private var currentSteps = 0

    companion object {
        const val CHANNEL_ID = "StepCounterChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_UPDATE_STEPS = "com.example.fitnessapp.UPDATE_STEPS"
        const val EXTRA_STEPS = "steps"

        fun start(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // WAŻNE: Musimy wywołać startForeground() NATYCHMIAST
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(0))

        // Inicjalizacja SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            // Urządzenie nie ma krokomierza sprzętowego
            // Ale NIE zatrzymujemy service tutaj - już wywołaliśmy startForeground()
            // Service będzie działał ale nie będzie liczył kroków
            return
        }

        // Rejestracja listenera
        sensorManager.registerListener(
            this,
            stepSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        loadDailySteps()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val totalSteps = it.values[0].toInt()

                // Przy pierwszym odczycie zapisujemy wartość bazową
                if (initialSteps == -1) {
                    initialSteps = totalSteps
                }

                // Obliczamy dzisiejsze kroki
                currentSteps = totalSteps - initialSteps

                // NOWE: Oblicz dystans, kalorie i czas aktywności
                calculateAndSaveMetrics(currentSteps)

                // Aktualizujemy notyfikację
                val notification = createNotification(currentSteps)
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)

                // Wysyłamy broadcast z aktualizacją kroków
                broadcastStepUpdate(currentSteps)
            }
        }
    }

    private fun calculateAndSaveMetrics(steps: Int) {
        val prefs = getSharedPreferences("fitness_prefs", MODE_PRIVATE)

        // Pobierz profil użytkownika
        val gender = prefs.getString("gender", "Mężczyzna") ?: "Mężczyzna"
        val height = prefs.getInt("height", 175)
        val weight = prefs.getFloat("weight", 70f)
        val age = prefs.getInt("age", 25)

        // 1. Oblicz dystans (na podstawie wzrostu i płci)
        val strideLength = if (gender == "Mężczyzna") {
            height * 0.415f / 100 // w metrach
        } else {
            height * 0.413f / 100
        }
        val distanceMeters = steps * strideLength
        val distanceKm = distanceMeters / 1000


        val activeTimeMinutes = (steps / 60).coerceAtLeast(0)

        // 3. Oblicz kalorie (MET method)
        val walkingMET = when {
            steps < 3000 -> 2.0f      // Powolne chodzenie
            steps < 7000 -> 3.5f      // Normalne chodzenie
            else -> 5.0f               // Szybkie chodzenie
        }
        val timeInHours = activeTimeMinutes / 60f
        val calories = (walkingMET * weight * timeInHours).toInt()

        // Zapisz wszystkie dane
        prefs.edit().apply {
            putInt("steps", steps)
            putFloat("distance", distanceKm)
            putInt("calories", calories)
            putInt("active_time", activeTimeMinutes)
            putLong("last_update", System.currentTimeMillis())
            apply()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun createNotification(steps: Int): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val sensorStatus = if (stepSensor == null) {
            " (brak sensora)"
        } else {
            ""
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fitness App")
            .setContentText("Dzisiaj: $steps kroków$sensorStatus")
            .setSmallIcon(R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Licznik kroków",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Wyświetla aktualną liczbę kroków"
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun broadcastStepUpdate(steps: Int) {
        val intent = Intent(ACTION_UPDATE_STEPS).apply {
            putExtra(EXTRA_STEPS, steps)
        }
        sendBroadcast(intent)
    }

    private fun loadDailySteps() {
        val prefs = getSharedPreferences("fitness_prefs", MODE_PRIVATE)

        // Sprawdź czy to nowy dzień używając Calendar
        val lastDate = prefs.getLong("last_step_date", 0)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = lastDate
        val lastDay = calendar.get(Calendar.DAY_OF_YEAR)
        val lastYear = calendar.get(Calendar.YEAR)

        if (currentYear > lastYear || currentDay > lastDay) {
            // Nowy dzień - resetuj wszystkie dane
            prefs.edit().apply {
                putInt("daily_steps", 0)
                putInt("steps", 0)
                putFloat("distance", 0f)
                putInt("calories", 0)
                putInt("active_time", 0)
                putLong("last_step_date", System.currentTimeMillis())
                putLong("last_update", System.currentTimeMillis())
                apply()
            }
            currentSteps = 0
            initialSteps = -1 // Reset initial steps
        } else {
            // Ten sam dzień - załaduj zapisane kroki
            currentSteps = prefs.getInt("daily_steps", 0)
        }
    }

    private fun saveDailySteps(steps: Int) {
        val prefs = getSharedPreferences("fitness_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("daily_steps", steps)
            putLong("last_step_date", System.currentTimeMillis())
            apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (stepSensor != null) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}