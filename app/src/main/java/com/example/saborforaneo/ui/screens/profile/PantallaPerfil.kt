package com.example.saborforaneo.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saborforaneo.R
import com.example.saborforaneo.ui.components.BarraNavegacionInferior
import com.example.saborforaneo.ui.screens.profile.componentes.*
import com.example.saborforaneo.ui.screens.profile.dialogos.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    navegarALogin: () -> Unit,
    controladorNav: NavController,
    modeloVista: PerfilViewModel = viewModel(),
    authViewModel: com.example.saborforaneo.viewmodel.AuthViewModel
) {
    val estado by modeloVista.estado.collectAsState()
    val estadoSnackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val context = LocalContext.current

    // Configurar Google Sign-In Client para poder cerrar sesión de Google
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Establecer el GoogleSignInClient en el AuthViewModel
    LaunchedEffect(googleSignInClient) {
        authViewModel.setGoogleSignInClient(googleSignInClient)
    }

    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    var mostrarDialogoEditarPerfil by remember { mutableStateOf(false) }
    var mostrarDialogoAcercaDe by remember { mutableStateOf(false) }
    var mostrarDialogoTerminos by remember { mutableStateOf(false) }
    var mostrarDialogoPrivacidad by remember { mutableStateOf(false) }
    var mostrarDialogoSelectorTema by remember { mutableStateOf(false) }
    var mostrarDialogoCambiarFoto by remember { mutableStateOf(false) }

    // Launcher para seleccionar imagen de galería
    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { imageUri ->
            // Subir la imagen a Firebase Storage
            modeloVista.actualizarFotoPerfil(imageUri) { exito, mensaje ->
                alcance.launch {
                    estadoSnackbar.showSnackbar(
                        message = if (exito) "✅ $mensaje" else "❌ $mensaje",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // Mostrar error si existe
    LaunchedEffect(estado.error) {
        estado.error?.let { mensaje ->
            estadoSnackbar.showSnackbar(
                message = mensaje,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(controladorNav = controladorNav)
        },
        snackbarHost = {
            SnackbarHost(hostState = estadoSnackbar)
        }
    ) { valoresPadding ->
        
        // Mostrar indicador de carga
        if (estado.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(valoresPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando perfil...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            return@Scaffold
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(valoresPadding),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ========== HEADER DEL PERFIL ==========
            item {
                SeccionPerfil(
                    nombreUsuario = estado.nombreUsuario,
                    correoUsuario = estado.correoUsuario,
                    fotoPerfil = estado.fotoPerfil,
                    alEditarPerfil = { mostrarDialogoEditarPerfil = true },
                    alCambiarFoto = { mostrarDialogoCambiarFoto = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ========== PREFERENCIAS ==========
            item {
                SeccionPreferencias(
                    modoTema = estado.modoTema,
                    temaColor = estado.temaColorSeleccionado,
                    alCambiarModoTema = { modo ->
                        modeloVista.cambiarModoTema(modo)
                        val mensaje = when (modo) {
                            ModoTema.AUTOMATICO -> "🔄 Tema automático (según sistema)"
                            ModoTema.CLARO -> "☀️ Tema claro activado"
                            ModoTema.OSCURO -> "🌙 Tema oscuro activado"
                        }
                        alcance.launch {
                            estadoSnackbar.showSnackbar(
                                message = mensaje,
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    alAbrirSelectorTema = { mostrarDialogoSelectorTema = true }
                )
            }

            item { DivisorSeccion() }

            // ========== PERMISOS ==========
            item {
                Text(
                    text = "Permisos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                SeccionPermisosNotificaciones(
                    notificacionesActivas = estado.notificacionesActivas,
                    onCambiarNotificaciones = { activado ->
                        modeloVista.cambiarNotificacionesActivas(activado)
                        alcance.launch {
                            estadoSnackbar.showSnackbar(
                                message = if (activado) "🔔 Notificaciones activadas" else "🔕 Notificaciones desactivadas",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }

            item {
                SeccionPermisosUbicacion(
                    ubicacionActiva = estado.ubicacionActiva,
                    onCambiarUbicacion = { activado ->
                        modeloVista.cambiarUbicacionActiva(activado)
                        alcance.launch {
                            estadoSnackbar.showSnackbar(
                                message = if (activado) "📍 Ubicación activada" else "📍 Ubicación desactivada",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }

            item { DivisorSeccion() }

            // ========== INFORMACIÓN ==========
            item {
                SeccionInformacion(
                    alAbrirAcercaDe = { mostrarDialogoAcercaDe = true },
                    alAbrirTerminos = { mostrarDialogoTerminos = true },
                    alAbrirPrivacidad = { mostrarDialogoPrivacidad = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ========== BOTÓN CERRAR SESIÓN ==========
            item {
                Button(
                    onClick = { mostrarDialogoCerrarSesion = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar Sesión",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ========================================
    // DIÁLOGOS
    // ========================================

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null
                )
            },
            title = { Text(text = "Cerrar Sesión") },
            text = { Text(text = "¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        // Cerrar sesión en Firebase (el AuthStateListener limpiará el estado automáticamente)
                        authViewModel.cerrarSesion()
                        // Navegar al login
                        navegarALogin()
                    }
                ) {
                    Text("Sí, cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoEditarPerfil) {
        DialogoEditarPerfil(
            nombreActual = estado.nombreUsuario,
            correoActual = estado.correoUsuario,
            alCerrar = { mostrarDialogoEditarPerfil = false },
            alGuardar = { nuevoNombre, nuevoCorreo, nuevaContrasena ->
                modeloVista.actualizarPerfil(nuevoNombre, nuevoCorreo, nuevaContrasena) { exito, mensaje ->
                    if (exito) {
                        mostrarDialogoEditarPerfil = false
                    }
                    alcance.launch {
                        estadoSnackbar.showSnackbar(
                            message = if (exito) "✅ $mensaje" else "❌ $mensaje",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        )
    }

    if (mostrarDialogoAcercaDe) {
        DialogoAcercaDe(alCerrar = { mostrarDialogoAcercaDe = false })
    }

    if (mostrarDialogoTerminos) {
        DialogoTerminos(alCerrar = { mostrarDialogoTerminos = false })
    }

    if (mostrarDialogoPrivacidad) {
        DialogoPrivacidad(alCerrar = { mostrarDialogoPrivacidad = false })
    }

    if (mostrarDialogoSelectorTema) {
        DialogoSelectorTema(
            temaActual = estado.temaColorSeleccionado,
            alCerrar = { mostrarDialogoSelectorTema = false },
            alSeleccionar = { nuevoTema ->
                modeloVista.cambiarTemaColor(nuevoTema)
                alcance.launch {
                    estadoSnackbar.showSnackbar(
                        message = "🎨 Tema ${nuevoTema.nombreMostrar} aplicado",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    if (mostrarDialogoCambiarFoto) {
        DialogoCambiarFoto(
            tieneFoto = estado.fotoPerfil.isNotEmpty(),
            alCerrar = { mostrarDialogoCambiarFoto = false },
            alSeleccionarGaleria = {
                selectorImagen.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            alEliminarFoto = {
                modeloVista.eliminarFotoPerfil { exito, mensaje ->
                    alcance.launch {
                        estadoSnackbar.showSnackbar(
                            message = if (exito) "✅ $mensaje" else "❌ $mensaje",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        )
    }
}