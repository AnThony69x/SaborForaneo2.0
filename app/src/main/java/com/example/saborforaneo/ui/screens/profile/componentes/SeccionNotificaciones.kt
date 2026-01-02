package com.example.saborforaneo.ui.screens.profile.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.saborforaneo.notifications.RecetaDiariaNotification
import com.example.saborforaneo.permissions.PermissionStatus
import com.example.saborforaneo.permissions.rememberNotificationPermissionState

@Composable
fun SeccionPermisosNotificaciones(
    notificacionesActivas: Boolean,
    onCambiarNotificaciones: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var mostrarDialogoPermiso by remember { mutableStateOf(false) }

    val permissionState = rememberNotificationPermissionState(
        onPermissionGranted = {
            onCambiarNotificaciones(true)
            RecetaDiariaNotification.mostrarNotificacionPrueba(context)
        },
        onPermissionDenied = {
            mostrarDialogoPermiso = true
            onCambiarNotificaciones(false)
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
                imageVector = Icons.Default.Notifications,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Notificaciones",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (notificacionesActivas) "Activadas" else "Desactivadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = notificacionesActivas,
            onCheckedChange = { activado ->
                if (activado) {
                    if (permissionState.status == PermissionStatus.GRANTED) {
                        onCambiarNotificaciones(true)
                        RecetaDiariaNotification.mostrarNotificacionPrueba(context)
                    } else {
                        permissionState.requestPermission()
                    }
                } else {
                    onCambiarNotificaciones(false)
                }
            }
        )
    }

    if (mostrarDialogoPermiso) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermiso = false },
            title = { Text("Permiso de Notificaciones") },
            text = { Text("Para recibir actualizaciones de recetas, ve a Configuración y habilita las notificaciones.") },
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