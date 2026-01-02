package com.example.saborforaneo.ui.screens.profile.dialogos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialogoCambiarFoto(
    tieneFoto: Boolean,
    alCerrar: () -> Unit,
    alSeleccionarGaleria: () -> Unit,
    alEliminarFoto: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCerrar,
        icon = {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null
            )
        },
        title = { Text("Foto de perfil") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Opción: Seleccionar de galería
                OutlinedCard(
                    onClick = {
                        alSeleccionarGaleria()
                        alCerrar()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Seleccionar de galería",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Elige una foto existente",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Opción: Eliminar foto (solo si tiene foto)
                if (tieneFoto) {
                    OutlinedCard(
                        onClick = {
                            alEliminarFoto()
                            alCerrar()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Column {
                                Text(
                                    text = "Eliminar foto",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Volver a la inicial",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = alCerrar) {
                Text("Cancelar")
            }
        }
    )
}
