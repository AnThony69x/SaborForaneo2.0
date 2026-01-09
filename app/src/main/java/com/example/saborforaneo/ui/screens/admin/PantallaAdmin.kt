package com.example.saborforaneo.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saborforaneo.R
import com.example.saborforaneo.viewmodel.AuthViewModel
import com.example.saborforaneo.viewmodel.AdminViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdmin(
    navegarALogin: () -> Unit,
    navegarAGestionRecetas: () -> Unit,
    navegarADashboard: () -> Unit,
    navegarAGestionUsuarios: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    perfilViewModel: com.example.saborforaneo.ui.screens.profile.PerfilViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val usuarioFirestore by authViewModel.usuarioFirestore.collectAsState()
    val context = LocalContext.current

    // ViewModel para estadísticas
    val adminViewModel = remember { AdminViewModel(context) }
    val estadisticas by adminViewModel.estadisticas.collectAsState()
    
    // Estados para animaciones y diálogo
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    var animacionVisible by remember { mutableStateOf(false) }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animacionVisible = true
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar del admin con gradiente
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Column {
                            Text(
                                text = "Panel Admin",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = usuarioFirestore?.nombre ?: "Administrador",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    // Botón de actualizar con animación
                    IconButton(
                        onClick = { adminViewModel.cargarEstadisticas() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Botón de cerrar sesión mejorado
                    FilledTonalButton(
                        onClick = { mostrarDialogoCerrarSesion = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Salir",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Banner de bienvenida con gradiente
            AnimatedVisibility(
                visible = animacionVisible,
                enter = slideInVertically(
                    initialOffsetY = { -100 },
                    animationSpec = tween(600)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¡Bienvenido de nuevo! 👋",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Gestiona tu aplicación desde aquí",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }

            // ========== ESTADÍSTICAS GENERALES ==========
            AnimatedVisibility(
                visible = animacionVisible,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(700, delayMillis = 100)
                ) + fadeIn(animationSpec = tween(700, delayMillis = 100))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "📊 Resumen",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

            if (estadisticas.cargando) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (estadisticas.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ Error al cargar datos",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = estadisticas.error ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { adminViewModel.cargarEstadisticas() }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else {
                // Tarjetas de estadísticas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaEstadistica(
                        icono = Icons.Default.Restaurant,
                        titulo = "Recetas",
                        valor = estadisticas.totalRecetas.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaEstadistica(
                        icono = Icons.Default.People,
                        titulo = "Usuarios",
                        valor = estadisticas.totalUsuarios.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaEstadistica(
                        icono = Icons.Default.AdminPanelSettings,
                        titulo = "Admins",
                        valor = estadisticas.totalAdmins.toString(),
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaEstadistica(
                        icono = Icons.AutoMirrored.Filled.TrendingUp,
                        titulo = "Hoy",
                        valor = "${estadisticas.recetasHoy}R / ${estadisticas.usuariosHoy}U",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Recetas más populares
                if (estadisticas.recetasMasPopulares.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "🔥 Recetas Más Populares",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            estadisticas.recetasMasPopulares.forEachIndexed { index, receta ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Medalla de posición
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = when (index) {
                                            0 -> MaterialTheme.colorScheme.primary
                                            1 -> MaterialTheme.colorScheme.secondary
                                            2 -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = when (index) {
                                                    0, 1, 2 -> MaterialTheme.colorScheme.onPrimary
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Nombre
                                    Text(
                                        text = receta.nombre,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Favoritos
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = receta.totalFavoritos.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                if (index < estadisticas.recetasMasPopulares.size - 1) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
                    }
                }
            }

            // ========== GESTIÓN ==========
            AnimatedVisibility(
                visible = animacionVisible,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(800, delayMillis = 300)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 300))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📋 Gestión",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            ItemAdministracion(
                                icono = Icons.Default.Restaurant,
                                titulo = "Gestionar Recetas",
                                descripcion = "Crear, editar y eliminar recetas",
                                onClick = navegarAGestionRecetas
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            
                            ItemAdministracion(
                                icono = Icons.Default.People,
                                titulo = "Gestionar Usuarios",
                                descripcion = "Ver usuarios y cambiar roles",
                                onClick = navegarAGestionUsuarios
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            
                            ItemAdministracion(
                                icono = Icons.Default.BarChart,
                                titulo = "Dashboard",
                                descripcion = "Estadísticas detalladas",
                                onClick = navegarADashboard
                            )
                        }
                    }
                }
            }

            // ========== HERRAMIENTAS ==========
            AnimatedVisibility(
                visible = animacionVisible,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(900, delayMillis = 400)
                ) + fadeIn(animationSpec = tween(900, delayMillis = 400))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🛠️ Herramientas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            ItemAdministracion(
                                icono = Icons.Default.Analytics,
                                titulo = "Reportes",
                                descripcion = "Próximamente",
                                onClick = null,
                                habilitado = false
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            
                            ItemAdministracion(
                                icono = Icons.Default.Notifications,
                                titulo = "Notificaciones Push",
                                descripcion = "Próximamente",
                                onClick = null,
                                habilitado = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Diálogo de confirmación para cerrar sesión
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
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "¿Estás seguro que deseas cerrar sesión?",
                            fontSize = 15.sp
                        )
                        
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Sesión actual:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = usuarioFirestore?.nombre ?: "Administrador",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentUser?.email ?: "",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    FilledTonalButton(
                        onClick = {
                            authViewModel.cerrarSesion()
                            mostrarDialogoCerrarSesion = false
                            navegarALogin()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
    }
}

@Composable
fun ItemAdministracion(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: (() -> Unit)?,
    habilitado: Boolean = true
) {
    Surface(
        onClick = { onClick?.invoke() },
        modifier = Modifier.fillMaxWidth(),
        enabled = habilitado && onClick != null,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono con fondo circular
            Surface(
                shape = CircleShape,
                color = if (habilitado) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = if (habilitado) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Texto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (habilitado) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )
                Text(
                    text = descripcion,
                    fontSize = 13.sp,
                    color = if (habilitado) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )
            }

            // Flecha si está habilitado
            if (habilitado && onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TarjetaEstadistica(
    icono: ImageVector,
    titulo: String,
    valor: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    var escalaAnimada by remember { mutableStateOf(0.8f) }
    
    LaunchedEffect(Unit) {
        escalaAnimada = 1f
    }
    
    val escala by animateFloatAsState(
        targetValue = escalaAnimada,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "escala"
    )
    
    ElevatedCard(
        modifier = modifier.scale(escala),
        colors = CardDefaults.elevatedCardColors(
            containerColor = color.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Text(
                text = titulo,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = valor,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}