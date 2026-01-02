package com.example.saborforaneo.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saborforaneo.util.Constantes
import com.example.saborforaneo.viewmodel.AuthState
import com.example.saborforaneo.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecuperarContrasena(
    navegarAtras: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var emailEnviado by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val authState by authViewModel.authState.collectAsState()

    // Observar el estado de autenticación
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                if (!emailEnviado) {
                    emailEnviado = true
                    authViewModel.resetAuthState()
                }
            }
            is AuthState.Error -> {
                mensajeError = state.message
            }
            else -> {}
        }
    }

    val cargando = authState is AuthState.Loading

    fun enviarEmailRecuperacion() {
        mensajeError = ""

        when {
            email.isBlank() -> {
                mensajeError = "Por favor ingresa tu email"
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> {
                mensajeError = "El formato del email no es válido"
            }
            else -> {
                authViewModel.recuperarContrasena(email.trim())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = navegarAtras) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = emailEnviado,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) togetherWith
                            fadeOut(animationSpec = tween(600))
                },
                label = "animacion"
            ) { enviado ->
                if (enviado) {
                    PantallaEmailEnviado(
                        email = email,
                        alVolverALogin = navegarAtras,
                        alReenviar = {
                            emailEnviado = false
                            mensajeError = ""
                        }
                    )
                } else {
                    PantallaIngresoEmail(
                        email = email,
                        onEmailChange = { email = it },
                        mensajeError = mensajeError,
                        cargando = cargando,
                        onEnviar = { enviarEmailRecuperacion() },
                        focusManager = focusManager
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaIngresoEmail(
    email: String,
    onEmailChange: (String) -> Unit,
    mensajeError: String,
    cargando: Boolean,
    onEnviar: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    var visible by remember { mutableStateOf(false) }
    var mensajeErrorLocal by remember { mutableStateOf("") }

    // Sincronizar error externo con local
    LaunchedEffect(mensajeError) {
        mensajeErrorLocal = mensajeError
    }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) +
                slideInVertically(initialOffsetY = { it / 4 })
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔐",
                fontSize = 80.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Olvidaste tu contraseña?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No te preocupes, ingresa tu email y te enviaremos un enlace para restablecer tu contraseña.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    onEmailChange(it)
                    mensajeErrorLocal = "" // Limpiar error al escribir
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
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onEnviar()
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = mensajeErrorLocal.isNotEmpty() || mensajeError.isNotEmpty()
            )

            if (mensajeErrorLocal.isNotEmpty() || mensajeError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (mensajeErrorLocal.isNotEmpty()) mensajeErrorLocal else mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onEnviar,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviar enlace",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Recibirás un email con instrucciones para restablecer tu contraseña.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaEmailEnviado(
    email: String,
    alVolverALogin: () -> Unit,
    alReenviar: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var escalaCheck by remember { mutableStateOf(0f) }

    val alcance = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(200)

        alcance.launch {
            animate(
                initialValue = 0f,
                targetValue = 1.2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) { value, _ ->
                escalaCheck = value
            }

            animate(
                initialValue = 1.2f,
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) { value, _ ->
                escalaCheck = value
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) +
                slideInVertically(initialOffsetY = { it / 4 })
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .size(120.dp)
                    .scale(escalaCheck)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¡Email enviado!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hemos enviado un enlace de recuperación a:",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = email,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Revisa tu bandeja de entrada",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Si no lo encuentras, revisa tu carpeta de spam o correo no deseado.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = alVolverALogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Volver al Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = alReenviar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿No recibiste el email? Reenviar",
                    fontSize = 14.sp
                )
            }
        }
    }
}