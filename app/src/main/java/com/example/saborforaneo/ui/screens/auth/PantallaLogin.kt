package com.example.saborforaneo.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saborforaneo.util.Constantes
import com.example.saborforaneo.viewmodel.AuthState
import com.example.saborforaneo.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin(
    navegarARegistro: () -> Unit,
    navegarAInicio: () -> Unit,
    navegarAAdmin: () -> Unit,
    navegarARecuperarContrasena: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var mostrarSnackbar by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val authState by authViewModel.authState.collectAsState()
    val esAdmin by authViewModel.esAdmin.collectAsState()

    // Observar el estado de autenticación
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                if (state.user != null) {
                    // Limpiar errores
                    mensajeError = ""
                    // Redirigir según el rol del usuario
                    if (esAdmin) {
                        navegarAAdmin()
                    } else {
                        navegarAInicio()
                    }
                    authViewModel.resetAuthState()
                }
            }
            is AuthState.Error -> {
                mensajeError = state.message
                mostrarSnackbar = true
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    val cargando = authState is AuthState.Loading

    fun iniciarSesion() {
        mensajeError = ""
        val emailTrim = email.trim()
        val passwordTrim = password.trim()

        when {
            emailTrim.isEmpty() || passwordTrim.isEmpty() -> {
                mensajeError = "Por favor completa todos los campos"
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches() -> {
                mensajeError = "El formato del email no es válido"
            }
            passwordTrim.length < 6 -> {
                mensajeError = "La contraseña debe tener al menos 6 caracteres"
            }
            else -> {
                authViewModel.iniciarSesion(emailTrim, passwordTrim)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🍳",
                fontSize = 80.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¡Bienvenido de vuelta!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inicia sesión para continuar",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    if (mensajeError.isNotEmpty()) mensajeError = ""
                },
                label = { Text("Email") },
                placeholder = { Text("tu@email.com") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Icono de email"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                isError = mensajeError.isNotEmpty() && mensajeError.contains("email", ignoreCase = true),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    if (mensajeError.isNotEmpty()) mensajeError = ""
                },
                label = { Text("Contraseña") },
                placeholder = { Text("••••••••") },
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
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        iniciarSesion()
                    }
                ),
                singleLine = true,
                isError = mensajeError.isNotEmpty() && mensajeError.contains("contraseña", ignoreCase = true),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (mensajeError.isNotEmpty()) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "¿Olvidaste tu contraseña?",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { navegarARecuperarContrasena() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { iniciarSesion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  O  ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿No tienes cuenta? ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Regístrate",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navegarARegistro() }
                )
            }
        }
    }
}