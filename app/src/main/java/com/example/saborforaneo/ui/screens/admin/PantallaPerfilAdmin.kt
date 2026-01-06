package com.example.saborforaneo.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saborforaneo.R
import com.example.saborforaneo.ui.components.BarraNavegacionInferiorAdmin
import com.example.saborforaneo.ui.screens.profile.componentes.*
import com.example.saborforaneo.ui.screens.profile.PerfilViewModel
import com.example.saborforaneo.ui.screens.profile.TemaColor
import com.example.saborforaneo.ui.screens.profile.ModoTema
import com.example.saborforaneo.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfilAdmin(
    navegarALogin: () -> Unit,
    controladorNav: NavController,
    authViewModel: AuthViewModel,
    perfilViewModel: PerfilViewModel
) {
    val estado by perfilViewModel.estado.collectAsState()
    val estadoSnackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val context = LocalContext.current

    // Configurar Google Sign-In Client
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    LaunchedEffect(googleSignInClient) {
        authViewModel.setGoogleSignInClient(googleSignInClient)
    }

    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    var mostrarDialogoEditarPerfil by remember { mutableStateOf(false) }
    var mostrarDialogoCambiarFoto by remember { mutableStateOf(false) }
    var mostrarDialogoSelectorTema by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil Administrador",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BarraNavegacionInferiorAdmin(controladorNav = controladorNav)
        },
        snackbarHost = {
            SnackbarHost(hostState = estadoSnackbar)
        }
    ) { valoresPadding ->
        
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
            // Header del perfil
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
                Spacer(modifier = Modifier.height(8.dp))
                // Badge de administrador
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cuenta Administrador",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Preferencias
            item {
                SeccionPreferencias(
                    modoTema = estado.modoTema,
                    temaColor = estado.temaColorSeleccionado,
                    alCambiarModoTema = { modo ->
                        perfilViewModel.cambiarModoTema(modo)
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

            item { 
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Configuración Admin
            item {
                Text(
                    text = "⚙️ Configuración de Administrador",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                ItemConfiguracion(
                    icono = Icons.Default.Notifications,
                    titulo = "Notificaciones de Admin",
                    descripcion = "Alertas importantes del sistema",
                    contenidoExtra = {
                        Switch(
                            checked = estado.notificacionesActivas,
                            onCheckedChange = { activado ->
                                perfilViewModel.cambiarNotificacionesActivas(activado)
                                alcance.launch {
                                    estadoSnackbar.showSnackbar(
                                        message = if (activado) "🔔 Notificaciones activadas" else "🔕 Notificaciones desactivadas",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                )
            }

            item {
                ItemConfiguracion(
                    icono = Icons.Default.Backup,
                    titulo = "Respaldo de Datos",
                    descripcion = "Exportar datos de la aplicación",
                    alHacerClic = {
                        alcance.launch {
                            estadoSnackbar.showSnackbar(
                                message = "🚧 Función próximamente disponible",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }

            item {
                ItemConfiguracion(
                    icono = Icons.Default.Security,
                    titulo = "Seguridad",
                    descripcion = "Configuración de seguridad avanzada",
                    alHacerClic = {
                        alcance.launch {
                            estadoSnackbar.showSnackbar(
                                message = "🚧 Función próximamente disponible",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botón cerrar sesión
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
                        imageVector = Icons.Default.Logout,
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

    // Diálogo de cerrar sesión
    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { 
                Text(
                    text = "Cerrar Sesión",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "¿Estás seguro que deseas cerrar sesión?")
                    Text(
                        text = "Se cerrará tu sesión de administrador",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        authViewModel.cerrarSesion()
                        navegarALogin()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
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

    // Diálogo de editar perfil
    if (mostrarDialogoEditarPerfil) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEditarPerfil = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            },
            title = { Text("Editar Perfil") },
            text = { 
                Text("Función de edición de perfil próximamente disponible") 
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoEditarPerfil = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo de cambiar foto
    if (mostrarDialogoCambiarFoto) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCambiarFoto = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null
                )
            },
            title = { Text("Cambiar Foto de Perfil") },
            text = { 
                Text("Función de cambio de foto próximamente disponible") 
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoCambiarFoto = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo selector de tema
    if (mostrarDialogoSelectorTema) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSelectorTema = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null
                )
            },
            title = { Text("Cambiar Tema") },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selecciona un color de tema:")
                    TemaColor.values().forEach { tema ->
                        TextButton(
                            onClick = {
                                perfilViewModel.cambiarTemaColor(tema)
                                alcance.launch {
                                    estadoSnackbar.showSnackbar(
                                        message = "🎨 Tema cambiado a ${tema.nombreMostrar}",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                mostrarDialogoSelectorTema = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(tema.nombreMostrar)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoSelectorTema = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
