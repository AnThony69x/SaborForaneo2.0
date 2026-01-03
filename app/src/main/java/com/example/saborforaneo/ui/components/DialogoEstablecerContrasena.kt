package com.example.saborforaneo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DialogoEstablecerContrasena(
    email: String,
    nombre: String,
    onConfirmar: (password: String) -> Unit,
    onCancelar: () -> Unit,
    mostrarLoading: Boolean = false
) {
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmarPasswordVisible by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    fun validarYConfirmar() {
        mensajeError = ""
        val passwordTrim = password.trim()
        val confirmarPasswordTrim = confirmarPassword.trim()

        when {
            passwordTrim.isEmpty() || confirmarPasswordTrim.isEmpty() -> {
                mensajeError = "Por favor completa todos los campos"
            }
            passwordTrim.length < 6 -> {
                mensajeError = "La contraseña debe tener al menos 6 caracteres"
            }
            passwordTrim != confirmarPasswordTrim -> {
                mensajeError = "Las contraseñas no coinciden"
            }
            else -> {
                onConfirmar(passwordTrim)
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!mostrarLoading) onCancelar() },
        properties = DialogProperties(
            dismissOnBackPress = !mostrarLoading,
            dismissOnClickOutside = !mostrarLoading
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔐",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Establece una contraseña",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hola, $nombre",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Para completar tu registro, establece una contraseña para tu cuenta.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (mensajeError.isNotEmpty()) mensajeError = ""
                    },
                    label = { Text("Contraseña") },
                    placeholder = { Text("Mínimo 6 caracteres") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Icono de contraseña"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible)
                                    "Ocultar contraseña"
                                else
                                    "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    enabled = !mostrarLoading,
                    isError = mensajeError.isNotEmpty() && mensajeError.contains("contraseña") && !mensajeError.contains("coinciden"),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmarPassword,
                    onValueChange = {
                        confirmarPassword = it
                        if (mensajeError.isNotEmpty()) mensajeError = ""
                    },
                    label = { Text("Confirmar contraseña") },
                    placeholder = { Text("Escribe la contraseña de nuevo") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Icono de contraseña"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmarPasswordVisible = !confirmarPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmarPasswordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (confirmarPasswordVisible)
                                    "Ocultar contraseña"
                                else
                                    "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (confirmarPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            validarYConfirmar()
                        }
                    ),
                    singleLine = true,
                    enabled = !mostrarLoading,
                    isError = mensajeError.isNotEmpty() && mensajeError.contains("coinciden"),
                    modifier = Modifier.fillMaxWidth()
                )

                if (mensajeError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f),
                        enabled = !mostrarLoading
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = { validarYConfirmar() },
                        modifier = Modifier.weight(1f),
                        enabled = !mostrarLoading
                    ) {
                        if (mostrarLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    }
}

