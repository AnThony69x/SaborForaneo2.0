package com.example.saborforaneo.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.saborforaneo.permissions.rememberLocationPermissionState
import com.example.saborforaneo.permissions.rememberNotificationPermissionState

@Composable
fun DialogoPermisosIniciales(
    mostrar: Boolean,
    onDismiss: () -> Unit
) {
    if (!mostrar) return

    val notificationPermission = rememberNotificationPermissionState()
    val locationPermission = rememberLocationPermissionState()

    LaunchedEffect(Unit) {
        notificationPermission.requestPermission()
        locationPermission.requestPermission()
        onDismiss()
    }
}