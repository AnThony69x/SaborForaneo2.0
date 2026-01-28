package com.example.saborforaneo.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saborforaneo.ui.components.BarraNavegacionInferiorAdmin
import com.example.saborforaneo.ui.navigation.Rutas
import com.example.saborforaneo.viewmodel.AdminViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicioAdmin(
    controladorNav: NavController,
    authViewModel: com.example.saborforaneo.viewmodel.AuthViewModel
) {
    val context = LocalContext.current
    val usuarioFirestore by authViewModel.usuarioFirestore.collectAsState()
    
    // ViewModel para estadísticas
    val adminViewModel = remember { AdminViewModel(context) }
    val estadisticas by adminViewModel.estadisticas.collectAsState()
    
    var animacionVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        animacionVisible = true
        adminViewModel.cargarEstadisticas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Panel Admin",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "¡Bienvenido ${usuarioFirestore?.nombre ?: "Administrador"}!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { adminViewModel.cargarEstadisticas() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BarraNavegacionInferiorAdmin(controladorNav = controladorNav)
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
            // Banner de bienvenida
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
                                    text = "Dashboard Principal 📊",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Resumen general de tu aplicación",
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

            // Estadísticas generales
            AnimatedVisibility(
                visible = animacionVisible,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(700, delayMillis = 100)
                ) + fadeIn(animationSpec = tween(700, delayMillis = 100))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "📊 Resumen General",
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
                        // Grid de estadísticas principales
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TarjetaEstadistica(
                                icono = Icons.Default.Restaurant,
                                titulo = "Total Recetas",
                                valor = estadisticas.totalRecetas.toString(),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            TarjetaEstadistica(
                                icono = Icons.Default.People,
                                titulo = "Total Usuarios",
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
                                titulo = "Administradores",
                                valor = estadisticas.totalAdmins.toString(),
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            TarjetaEstadistica(
                                icono = Icons.Default.TrendingUp,
                                titulo = "Nuevos Hoy",
                                valor = "${estadisticas.recetasHoy + estadisticas.usuariosHoy}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Recetas más populares
                        if (estadisticas.recetasMasPopulares.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "🔥 Top 5 Recetas Populares",
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

            // Acciones Rápidas de Administración
            AnimatedVisibility(
                visible = animacionVisible,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(800, delayMillis = 200)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "⚡ Acciones Rápidas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Gestión de Usuarios
                    Card(
                        onClick = {
                            controladorNav.navigate(Rutas.GestionUsuarios.ruta)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gestión de Usuarios",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Banear, eliminar o gestionar usuarios",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Moderar Recetas
                    Card(
                        onClick = {
                            controladorNav.navigate(Rutas.Moderacion.ruta)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Moderar Recetas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Aprobar o rechazar recetas pendientes",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Gestión de Recetas
                    Card(
                        onClick = {
                            controladorNav.navigate(Rutas.GestionRecetas.ruta)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gestión de Recetas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Añadir, editar o eliminar recetas",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
