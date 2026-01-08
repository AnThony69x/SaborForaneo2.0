package com.example.saborforaneo.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saborforaneo.R
import com.example.saborforaneo.ui.components.DialogoEstablecerContrasena
import com.example.saborforaneo.util.ValidacionConstantes
import com.example.saborforaneo.viewmodel.AuthState
import com.example.saborforaneo.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(
    navegarAtras: () -> Unit,
    navegarAInicio: () -> Unit,
    navegarAAdmin: () -> Unit,
    navegarATerminos: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmarPasswordVisible by remember { mutableStateOf(false) }
    var aceptaTerminos by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    // Estados para el diálogo de contraseña de Google
    var mostrarDialogoContrasena by remember { mutableStateOf(false) }
    var datosGooglePendientes by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val authState by authViewModel.authState.collectAsState()
    val esAdmin by authViewModel.esAdmin.collectAsState()

    // Configurar Google Sign-In
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Establecer el GoogleSignInClient en el ViewModel
    LaunchedEffect(googleSignInClient) {
        authViewModel.setGoogleSignInClient(googleSignInClient)
    }

    // Launcher para el inicio de sesión con Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    authViewModel.iniciarSesionConGoogle(idToken)
                }
            } catch (e: ApiException) {
                mensajeError = "Error al registrarse con Google: ${e.message}"
            }
        }
    }

    // Observar el estado de autenticación
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                if (state.user != null) {
                    mensajeError = ""
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
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
            }
            is AuthState.NecesitaContrasena -> {
                datosGooglePendientes = Triple(state.email, state.nombre, state.idToken)
                mostrarDialogoContrasena = true
                authViewModel.resetAuthState()
            }
            else -> {}
        }
    }

    val cargando = authState is AuthState.Loading

    fun registrarse() {
        mensajeError = ""
        val nombreTrim = nombre.trim()
        val emailTrim = email.trim()
        val passwordTrim = password.trim()
        val confirmarPasswordTrim = confirmarPassword.trim()

        when {
            nombreTrim.isEmpty() || emailTrim.isEmpty() || passwordTrim.isEmpty() || confirmarPasswordTrim.isEmpty() -> {
                mensajeError = "Por favor completa todos los campos"
            }
            nombreTrim.length < 3 -> {
                mensajeError = "El nombre debe tener al menos 3 caracteres"
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches() -> {
                mensajeError = "El formato del email no es válido"
            }
            passwordTrim.length < 6 -> {
                mensajeError = "La contraseña debe tener al menos 6 caracteres"
            }
            passwordTrim != confirmarPasswordTrim -> {
                mensajeError = "Las contraseñas no coinciden"
            }
            !aceptaTerminos -> {
                mensajeError = "Debes aceptar los términos y condiciones para continuar"
            }
            else -> {
                authViewModel.registrarUsuario(nombreTrim, emailTrim, passwordTrim)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta") },
                navigationIcon = {
                    IconButton(onClick = navegarAtras) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🍳",
                fontSize = 70.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Únete a SaborForáneo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crea tu cuenta para empezar",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    if (it.length <= ValidacionConstantes.NOMBRE_USUARIO_MAX) {
                        nombre = it
                        if (mensajeError.isNotEmpty()) mensajeError = ""
                    }
                },
                label = { Text("Nombre completo") },
                placeholder = { Text("Juan Pérez") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Icono de persona"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !cargando,
                isError = mensajeError.isNotEmpty() && mensajeError.contains("nombre", ignoreCase = true),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (it.length <= ValidacionConstantes.EMAIL_MAX) {
                        email = it
                        if (mensajeError.isNotEmpty()) mensajeError = ""
                    }
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
                enabled = !cargando,
                isError = mensajeError.isNotEmpty() && mensajeError.contains("email", ignoreCase = true),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (it.length <= ValidacionConstantes.PASSWORD_MAX) {
                        password = it
                        if (mensajeError.isNotEmpty()) mensajeError = ""
                    }
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
                enabled = !cargando,
                isError = mensajeError.isNotEmpty() && mensajeError.contains("contraseña", ignoreCase = true) && !mensajeError.contains("coinciden"),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmarPassword,
                onValueChange = {
                    if (it.length <= ValidacionConstantes.PASSWORD_MAX) {
                        confirmarPassword = it
                        if (mensajeError.isNotEmpty()) mensajeError = ""
                    }
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
                    }
                ),
                singleLine = true,
                enabled = !cargando,
                isError = mensajeError.isNotEmpty() && mensajeError.contains("coinciden"),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = aceptaTerminos,
                    onCheckedChange = { aceptaTerminos = it },
                    enabled = !cargando
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acepto los ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "términos y condiciones",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navegarATerminos() }
                )
            }

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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { registrarse() },
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
                        text = "Crear Cuenta",
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

            // Botón de Google Sign-In
            OutlinedButton(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !cargando,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "G",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continuar con Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿Ya tienes cuenta? ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Inicia sesión",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navegarAtras() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Mostrar diálogo para establecer contraseña si es necesario
    if (mostrarDialogoContrasena && datosGooglePendientes != null) {
        val (emailGoogle, nombreGoogle, idTokenGoogle) = datosGooglePendientes!!
        DialogoEstablecerContrasena(
            email = emailGoogle,
            nombre = nombreGoogle,
            onConfirmar = { passwordNueva ->
                authViewModel.completarRegistroConGoogle(
                    email = emailGoogle,
                    nombre = nombreGoogle,
                    password = passwordNueva,
                    idToken = idTokenGoogle
                )
            },
            onCancelar = {
                mostrarDialogoContrasena = false
                datosGooglePendientes = null
            },
            mostrarLoading = authState is AuthState.Loading
        )
    }
}

