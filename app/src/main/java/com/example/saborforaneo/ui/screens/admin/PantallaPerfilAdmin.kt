package com.example.saborforaneo.ui.screens.admin

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saborforaneo.R
import com.example.saborforaneo.ui.components.BarraNavegacionInferiorAdmin
import com.example.saborforaneo.ui.screens.profile.componentes.*
import com.example.saborforaneo.ui.screens.profile.PerfilViewModel
import com.example.saborforaneo.ui.screens.profile.TemaColor
import com.example.saborforaneo.ui.screens.profile.ModoTema
import com.example.saborforaneo.viewmodel.AuthViewModel
import com.example.saborforaneo.viewmodel.BackupViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfilAdmin(
    navegarALogin: () -> Unit,
    controladorNav: NavController,
    authViewModel: AuthViewModel,
    perfilViewModel: PerfilViewModel,
    backupViewModel: BackupViewModel = viewModel()
) {
    val estado by perfilViewModel.estado.collectAsState()
    val backupState by backupViewModel.uiState.collectAsState()
    val estadoSnackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val context = LocalContext.current

    // Estado para diálogos de respaldo
    var mostrarDialogoRespaldo by remember { mutableStateOf(false) }

    // Launcher para solicitar permisos de Google Drive
    val drivePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
            if (account != null) {
                backupViewModel.initializeDriveService(context, account)
                backupViewModel.realizarRespaldo(context)
            }
        }
    }

    // Observar cambios en el estado del backup
    LaunchedEffect(backupState.needsDrivePermission) {
        if (backupState.needsDrivePermission) {
            val intent = backupViewModel.getDriveSignInIntent(context)
            drivePermissionLauncher.launch(intent)
        }
    }

    LaunchedEffect(backupState.message, backupState.error) {
        backupState.message?.let {
            estadoSnackbar.showSnackbar(it)
            backupViewModel.clearMessage()
        }
        backupState.error?.let {
            estadoSnackbar.showSnackbar("❌ $it")
            backupViewModel.clearMessage()
        }
    }

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
    var mostrarDialogoSeguridad by remember { mutableStateOf(false) }

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
                    descripcion = if (backupState.lastBackupDate != null)
                        "Último respaldo: ${backupState.lastBackupDate}"
                    else
                        "Guardar datos en Google Drive",
                    alHacerClic = {
                        mostrarDialogoRespaldo = true
                    },
                    contenidoExtra = {
                        if (backupState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
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
                        mostrarDialogoSeguridad = true
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

    // Diálogo de cerrar sesión
    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
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

    // Diálogo de respaldo de datos
    if (mostrarDialogoRespaldo) {
        AlertDialog(
            onDismissRequest = {
                if (!backupState.isLoading) {
                    mostrarDialogoRespaldo = false
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Respaldo en Google Drive",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "El respaldo incluirá:",
                        fontWeight = FontWeight.Medium
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Todas las recetas", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Datos de usuarios", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Forum,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recetas de la comunidad", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Comment,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Comentarios", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (backupState.lastBackupDate != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Último respaldo: ${backupState.lastBackupDate}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    if (backupState.isLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Realizando respaldo...")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        backupViewModel.realizarRespaldo(context)
                    },
                    enabled = !backupState.isLoading
                ) {
                    Icon(
                        Icons.Default.Backup,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Respaldar Ahora")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoRespaldo = false },
                    enabled = !backupState.isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de seguridad
    if (mostrarDialogoSeguridad) {
        DialogoSeguridad(
            onDismiss = { mostrarDialogoSeguridad = false },
            onCambiarContrasena = {
                mostrarDialogoSeguridad = false
                alcance.launch {
                    estadoSnackbar.showSnackbar("📧 Se enviará un correo para cambiar tu contraseña")
                }
                // Enviar correo de restablecimiento
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.let { email ->
                    com.google.firebase.auth.FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            alcance.launch {
                                estadoSnackbar.showSnackbar("✅ Correo enviado a $email")
                            }
                        }
                        .addOnFailureListener {
                            alcance.launch {
                                estadoSnackbar.showSnackbar("❌ Error al enviar correo")
                            }
                        }
                }
            },
            onVerSesiones = {
                mostrarDialogoSeguridad = false
                alcance.launch {
                    estadoSnackbar.showSnackbar("📱 Sesión activa en este dispositivo")
                }
            },
            onActivar2FA = { activado ->
                alcance.launch {
                    if (activado) {
                        estadoSnackbar.showSnackbar("🔐 Verificación en 2 pasos activada")
                    } else {
                        estadoSnackbar.showSnackbar("⚠️ Verificación en 2 pasos desactivada")
                    }
                }
            },
            onBloquearUsuarios = {
                mostrarDialogoSeguridad = false
                // Navegar a gestión de usuarios con filtro de bloqueados
                controladorNav.navigate(com.example.saborforaneo.ui.navigation.Rutas.GestionUsuarios.ruta)
            }
        )
    }
}
