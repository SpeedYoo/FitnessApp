package com.example.fitnessapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Klasa do zarządzania uprawnieniami aplikacji
 */
class PermissionHandler(
    private val activity: ComponentActivity,
    private val onAllPermissionsGranted: () -> Unit
) {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var pendingPermissions = mutableListOf<String>()

    init {
        setupPermissionLauncher()
    }

    private fun setupPermissionLauncher() {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted || hasMinimumPermissions()) {
                onAllPermissionsGranted()
            }
        }
    }

    /**
     * Żąda wszystkich wymaganych uprawnień
     */
    fun requestPermissions() {
        val permissionsToRequest = getRequiredPermissions()
            .filter { !isPermissionGranted(it) }

        if (permissionsToRequest.isEmpty()) {
            onAllPermissionsGranted()
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    /**
     * Zwraca listę wymaganych uprawnień w zależności od wersji Androida
     */
    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        // Uprawnienie dla krokomierza (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        // Uprawnienia GPS
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Uprawnienie dla notyfikacji (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Uprawnienie dla Foreground Service Location (Android 14+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }

        return permissions
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Sprawdza czy mamy minimalne uprawnienia do działania
     */
    private fun hasMinimumPermissions(): Boolean {
        // Minimalne uprawnienia: lokalizacja (dla GPS w treningach)
        return isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    companion object {
        /**
         * Sprawdza czy dane uprawnienie jest przyznane
         */
        fun hasPermission(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * Sprawdza czy mamy uprawnienie do krokomierza
         */
        fun hasActivityRecognition(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hasPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                true
            }
        }

        /**
         * Sprawdza czy mamy uprawnienie do lokalizacji
         */
        fun hasLocationPermission(context: Context): Boolean {
            return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
}