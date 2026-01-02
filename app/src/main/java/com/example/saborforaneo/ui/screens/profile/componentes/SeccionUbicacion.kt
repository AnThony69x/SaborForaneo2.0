package com.example.saborforaneo.ui.screens.profile.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.saborforaneo.permissions.PermissionStatus
import com.example.saborforaneo.permissions.rememberLocationPermissionState

@Composable
fun SeccionPermisosUbicacion(
    ubicacionActiva: Boolean,
    onCambiarUbicacion: (Boolean) -> Unit
) {
    var mostrarDialogoPermiso by remember { mutableStateOf(false) }

    val permissionState = rememberLocationPermissionState(
        onPermissionGranted = {
            onCambiarUbicacion(true)
        },
        onPermissionDenied = {
            mostrarDialogoPermiso = true
            onCambiarUbicacion(false)
        }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (ubicacionActiva) "Recetas locales activas" else "Desactivada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = ubicacionActiva,
            onCheckedChange = { activado ->
                if (activado) {
                    if (permissionState.status == PermissionStatus.GRANTED) {
                        onCambiarUbicacion(true)
                    } else {
                        permissionState.requestPermission()
                    }
                } else {
                    onCambiarUbicacion(false)
                }
            }
        )
    }

    if (mostrarDialogoPermiso) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermiso = false },
            title = { Text("Permiso de Ubicación") },
            text = { Text("Para mostrarte recetas de tu región, ve a Configuración y habilita la ubicación.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        permissionState.openSettings()
                        mostrarDialogoPermiso = false
                    }
                ) {
                    Text("Ir a Configuración")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPermiso = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}