package com.example.saborforaneo.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoSeguridad(
    onDismiss: () -> Unit,
    onCambiarContrasena: () -> Unit,
    onVerSesiones: () -> Unit,
    onActivar2FA: (Boolean) -> Unit,
    onBloquearUsuarios: () -> Unit,
    is2FAEnabled: Boolean = false
) {
    var dobleFactorActivo by remember { mutableStateOf(is2FAEnabled) }
    var mostrarConfirmacion2FA by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Configuración de Seguridad",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sección: Autenticación
                Text(
                    text = "🔐 Autenticación",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Cambiar contraseña
                Card(
                    onClick = onCambiarContrasena,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cambiar Contraseña",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Actualiza tu contraseña de acceso",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }

                // Autenticación de dos factores
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhonelinkLock,
                            contentDescription = null,
                            tint = if (dobleFactorActivo)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Verificación en 2 pasos",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (dobleFactorActivo) "Activada" else "Desactivada",
                                fontSize = 12.sp,
                                color = if (dobleFactorActivo)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = dobleFactorActivo,
                            onCheckedChange = {
                                mostrarConfirmacion2FA = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sección: Control de Acceso
                Text(
                    text = "👥 Control de Acceso",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Sesiones activas
                Card(
                    onClick = onVerSesiones,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Devices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sesiones Activas",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Ver dispositivos conectados",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }

                // Usuarios bloqueados
                Card(
                    onClick = onBloquearUsuarios,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Usuarios Bloqueados",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Gestionar usuarios restringidos",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sección: Información
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "La seguridad de tu cuenta es importante. Mantén tu contraseña segura y activa la verificación en 2 pasos.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )

    // Diálogo de confirmación para 2FA
    if (mostrarConfirmacion2FA) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion2FA = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.PhonelinkLock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = if (dobleFactorActivo) "Desactivar 2FA" else "Activar 2FA",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (dobleFactorActivo)
                        "¿Estás seguro de desactivar la verificación en 2 pasos? Tu cuenta será menos segura."
                    else
                        "La verificación en 2 pasos añade una capa extra de seguridad. Recibirás un código por correo cada vez que inicies sesión."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        dobleFactorActivo = !dobleFactorActivo
                        onActivar2FA(dobleFactorActivo)
                        mostrarConfirmacion2FA = false
                    },
                    colors = if (dobleFactorActivo)
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    else
                        ButtonDefaults.buttonColors()
                ) {
                    Text(if (dobleFactorActivo) "Desactivar" else "Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion2FA = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
