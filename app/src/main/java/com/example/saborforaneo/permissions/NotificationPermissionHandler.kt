package com.example.saborforaneo.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberNotificationPermissionState(
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {}
): NotificationPermissionState {
    val context = LocalContext.current

    var permissionStatus by remember {
        mutableStateOf(checkNotificationPermission(context))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionStatus = if (isGranted) {
            onPermissionGranted()
            PermissionStatus.GRANTED
        } else {
            onPermissionDenied()
            PermissionStatus.DENIED
        }
    }

    return NotificationPermissionState(
        status = permissionStatus,
        requestPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                permissionStatus = PermissionStatus.GRANTED
                onPermissionGranted()
            }
        },
        openSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    )
}

data class NotificationPermissionState(
    val status: PermissionStatus,
    val requestPermission: () -> Unit,
    val openSettings: () -> Unit
)

fun checkNotificationPermission(context: Context): PermissionStatus {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        when (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        )) {
            PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
            else -> PermissionStatus.NOT_REQUESTED
        }
    } else {
        PermissionStatus.GRANTED
    }
}