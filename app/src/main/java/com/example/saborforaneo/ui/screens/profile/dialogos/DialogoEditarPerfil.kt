package com.example.saborforaneo.ui.screens.profile.dialogos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogoEditarPerfil(
    nombreActual: String,
    correoActual: String,
    alCerrar: () -> Unit,
    alGuardar: (String, String, String?) -> Unit
) {
    var nombre by remember { mutableStateOf(nombreActual) }
    var correo by remember { mutableStateOf(correoActual) }
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmarContrasena by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!guardando) alCerrar() },
        icon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null
            )
        },
        title = { Text("Editar Perfil") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        mensajeError = ""
                    },
                    label = { Text("Nombre completo") },
                    placeholder = { Text("Tu nombre") },
                    singleLine = true,
                    enabled = !guardando,
                    isError = mensajeError.contains("nombre", ignoreCase = true),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Correo (deshabilitado)
                OutlinedTextField(
                    value = correo,
                    onValueChange = { },
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("tu@correo.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "El correo no se puede modificar",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Divisor
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Texto de cambiar contraseña
                Text(
                    text = "Cambiar contraseña (opcional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Nueva contraseña
                OutlinedTextField(
                    value = nuevaContrasena,
                    onValueChange = {
                        nuevaContrasena = it
                        mensajeError = ""
                    },
                    label = { Text("Nueva contraseña") },
                    placeholder = { Text("Dejar vacío para no cambiar") },
                    visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                            Icon(
                                imageVector = if (mostrarContrasena) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    singleLine = true,
                    enabled = !guardando,
                    isError = mensajeError.contains("contraseña", ignoreCase = true),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Confirmar contraseña
                OutlinedTextField(
                    value = confirmarContrasena,
                    onValueChange = {
                        confirmarContrasena = it
                        mensajeError = ""
                    },
                    label = { Text("Confirmar contraseña") },
                    placeholder = { Text("Repite la nueva contraseña") },
                    visualTransformation = if (mostrarConfirmarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { mostrarConfirmarContrasena = !mostrarConfirmarContrasena }) {
                            Icon(
                                imageVector = if (mostrarConfirmarContrasena) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (mostrarConfirmarContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    singleLine = true,
                    enabled = !guardando && nuevaContrasena.isNotEmpty(),
                    isError = mensajeError.contains("coinciden", ignoreCase = true),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (nuevaContrasena.isNotEmpty()) {
                    Text(
                        text = "La contraseña debe tener al menos 6 caracteres",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                    )
                }
                
                // Mensaje de error
                if (mensajeError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val nombreTrim = nombre.trim()
                    val contrasenaTrim = nuevaContrasena.trim()
                    
                    when {
                        nombreTrim.isEmpty() -> {
                            mensajeError = "El nombre no puede estar vacío"
                        }
                        nombreTrim.length < 3 -> {
                            mensajeError = "El nombre debe tener al menos 3 caracteres"
                        }
                        contrasenaTrim.isNotEmpty() && contrasenaTrim.length < 6 -> {
                            mensajeError = "La contraseña debe tener al menos 6 caracteres"
                        }
                        contrasenaTrim.isNotEmpty() && contrasenaTrim != confirmarContrasena.trim() -> {
                            mensajeError = "Las contraseñas no coinciden"
                        }
                        else -> {
                            guardando = true
                            val passwordFinal = if (contrasenaTrim.isEmpty()) null else contrasenaTrim
                            alGuardar(nombreTrim, correoActual, passwordFinal)
                        }
                    }
                },
                enabled = !guardando
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = alCerrar,
                enabled = !guardando
            ) {
                Text("Cancelar")
            }
        }
    )
}
