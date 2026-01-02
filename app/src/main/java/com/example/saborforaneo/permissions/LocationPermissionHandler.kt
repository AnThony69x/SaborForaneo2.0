package com.example.saborforaneo.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberLocationPermissionState(
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {}
): LocationPermissionState {
    val context = LocalContext.current

    var permissionStatus by remember {
        mutableStateOf(checkLocationPermission(context))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        permissionStatus = if (granted) {
            onPermissionGranted()
            PermissionStatus.GRANTED
        } else {
            onPermissionDenied()
            PermissionStatus.DENIED
        }
    }

    return LocationPermissionState(
        status = permissionStatus,
        requestPermission = {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        },
        openSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    )
}

data class LocationPermissionState(
    val status: PermissionStatus,
    val requestPermission: () -> Unit,
    val openSettings: () -> Unit
)

fun checkLocationPermission(context: Context): PermissionStatus {
    return when {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
        else -> PermissionStatus.NOT_REQUESTED
    }
}