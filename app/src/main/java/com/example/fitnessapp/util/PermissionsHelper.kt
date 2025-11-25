package com.example.fitnessapp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Helper do zarządzania uprawnieniami wymaganymi przez aplikację
 */
object PermissionsHelper {

    // Uprawnienia potrzebne dla krokomierza
    val STEP_COUNTER_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
    } else {
        emptyArray()
    }

    // Uprawnienia potrzebne dla GPS (treningi)
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Uprawnienie dla notyfikacji (Android 13+)
    val NOTIFICATION_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    // Sprawdzenie czy uprawnienia są przyznane
    fun hasStepCounterPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Starsze wersje Androida nie wymagają tego uprawnienia
        }
    }

    fun hasLocationPermissions(context: Context): Boolean {
        return LOCATION_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Prośba o uprawnienia w Activity
    fun requestStepCounterPermission(
        activity: ComponentActivity,
        onResult: (Boolean) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val requestPermission = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                onResult(isGranted)
            }
            requestPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            onResult(true)
        }
    }

    fun requestLocationPermissions(
        activity: ComponentActivity,
        onResult: (Boolean) -> Unit
    ) {
        val requestPermissions = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            onResult(allGranted)
        }
        requestPermissions.launch(LOCATION_PERMISSIONS)
    }

    fun requestNotificationPermission(
        activity: ComponentActivity,
        onResult: (Boolean) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestPermission = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                onResult(isGranted)
            }
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onResult(true)
        }
    }
}