package com.example.saborforaneo.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.saborforaneo.ui.components.BarraNavegacionInferior
import com.example.saborforaneo.ui.components.ChipFiltro
import com.example.saborforaneo.ui.components.TarjetaReceta
import com.example.saborforaneo.ui.components.TarjetaRecetaSkeleton
import com.example.saborforaneo.ui.components.DialogoRequiereAuth
import com.example.saborforaneo.ui.components.MensajesAuth
import com.example.saborforaneo.ui.navigation.Rutas
import com.example.saborforaneo.ui.screens.chat.PantallaChat
import com.example.saborforaneo.viewmodel.HomeViewModel
import com.example.saborforaneo.util.Categorias
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    navegarADetalle: (String) -> Unit,
    navegarABusqueda: () -> Unit,
    controladorNav: NavController,
    homeViewModel: HomeViewModel
) {
    val contexto = LocalContext.current

    // Usar el ViewModel compartido
    val uiState by homeViewModel.uiState.collectAsState()

    // Recargar favoritos cada vez que navegas de regreso a esta pantalla
    LaunchedEffect(Unit) {
        homeViewModel.recargarFavoritos()
    }

    // Estado para controlar el diálogo del chat
    var mostrarChat by remember { mutableStateOf(false) }

    // Estado para diálogo de autenticación requerida
    var mostrarDialogoAuth by remember { mutableStateOf(false) }
    var tipoDialogoAuth by remember { mutableStateOf("chat") } // "chat" o "favoritos"

    // Verificar autenticación
    val usuarioActual = FirebaseAuth.getInstance().currentUser
    val estaAutenticado = usuarioActual != null

    var ubicacionTexto by remember { mutableStateOf<String?>(null) }

    val tienePermisoUbicacion = remember {
        ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(key1 = tienePermisoUbicacion) {
        if (tienePermisoUbicacion) {
            try {
                val locationManager = contexto.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager

                @SuppressLint("MissingPermission")
                val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if (location != null) {
                    val geocoder = Geocoder(contexto, Locale.getDefault())

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0]
                                val ciudad = address.locality ?: address.subAdminArea
                                val provincia = address.adminArea
                                ubicacionTexto = if (ciudad != null && provincia != null) {
                                    "$ciudad, $provincia"
                                } else {
                                    provincia ?: "Ecuador"
                                }
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val ciudad = address.locality ?: address.subAdminArea
                            val provincia = address.adminArea
                            ubicacionTexto = if (ciudad != null && provincia != null) {
                                "$ciudad, $provincia"
                            } else {
                                provincia ?: "Ecuador"
                            }
                        }
                    }
                } else {
                    ubicacionTexto = "Ecuador"
                }
            } catch (e: Exception) {
                ubicacionTexto = "Ecuador"
            }
        }
    }

    // Cargar categorías predefinidas (sin "Todas" porque se muestra manualmente)
    val categorias = remember { Categorias.lista.filter { it.nombre != "Todas" } }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }

    // Filtrar recetas según categoría seleccionada
    val recetasFiltradas = remember(categoriaSeleccionada, uiState.recetas) {
        homeViewModel.obtenerRecetasPorCategoria(categoriaSeleccionada)
    }

    // Animaciones para el botón flotante
    val infiniteTransition = rememberInfiniteTransition(label = "float_animation")

    // Animación de escala (pulsación)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Animación de rotación suave
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SaborForáneo",
                            fontWeight = FontWeight.Bold
                        )
                        if (tienePermisoUbicacion && ubicacionTexto != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = ubicacionTexto ?: "Ecuador",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            Text(
                                text = "¿Qué cocinarás hoy?",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {

                    IconButton(onClick = navegarABusqueda) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Buscar recetas"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(controladorNav = controladorNav)
        }
    ) { paddingValues ->
        if (uiState.cargando) {
            // Mostrar skeletons mientras carga
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(5) {
                    TarjetaRecetaSkeleton(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        } else if (uiState.error != null) {
            // Mostrar error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Error al cargar recetas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Button(onClick = { homeViewModel.cargarRecetas() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else {
            // Mostrar recetas con animación
            AnimatedVisibility(
                visible = !uiState.cargando,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = "Categorías",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    ChipFiltro(
                                        texto = "Todas",
                                        seleccionado = categoriaSeleccionada == null,
                                        alSeleccionar = { categoriaSeleccionada = null }
                                    )
                                }

                                items(categorias) { categoria ->
                                    ChipFiltro(
                                        texto = "${categoria.icono} ${categoria.nombre}",
                                        seleccionado = categoriaSeleccionada == categoria.nombre,
                                        alSeleccionar = {
                                            categoriaSeleccionada = if (categoriaSeleccionada == categoria.nombre) {
                                                null
                                            } else {
                                                categoria.nombre
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = if (categoriaSeleccionada == null)
                                "Recetas Destacadas (${recetasFiltradas.size})"
                            else
                                "Recetas de $categoriaSeleccionada (${recetasFiltradas.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (recetasFiltradas.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🍽️",
                                        fontSize = 64.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No hay recetas disponibles",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Intenta con otra categoría",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(recetasFiltradas) { index, receta ->
                            var visible by remember { mutableStateOf(false) }

                            LaunchedEffect(key1 = receta.id) {
                                delay(index * 50L)
                                visible = true
                            }

                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(300)) +
                                        slideInVertically(
                                            initialOffsetY = { it / 2 },
                                            animationSpec = tween(300)
                                        )
                            ) {
                                TarjetaReceta(
                                    receta = receta,
                                    alHacerClick = { navegarADetalle(receta.id) },
                                    esFavorito = receta.esFavorito,
                                    onToggleFavorito = { homeViewModel.toggleFavorito(it) },
                                    onRequiereAuth = {
                                        tipoDialogoAuth = "favoritos"
                                        mostrarDialogoAuth = true
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        // Botón flotante animado para el Chat con IA (más pequeño, en la parte inferior)
        FloatingActionButton(
            onClick = {
                if (estaAutenticado) {
                    mostrarChat = true
                } else {
                    tipoDialogoAuth = "chat"
                    mostrarDialogoAuth = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 16.dp)
                .size(48.dp)
                .scale(scale)
                .rotate(rotation),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "Chat con Chef AI",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Diálogo de autenticación requerida para el Chat o Favoritos
    if (mostrarDialogoAuth) {
        val mensajes = if (tipoDialogoAuth == "chat") MensajesAuth.ASISTENTE else MensajesAuth.FAVORITOS
        DialogoRequiereAuth(
            titulo = mensajes.first,
            mensaje = mensajes.second,
            emoji = mensajes.third,
            onDismiss = { mostrarDialogoAuth = false },
            onIniciarSesion = {
                mostrarDialogoAuth = false
                controladorNav.navigate(Rutas.Login.ruta)
            },
            onRegistrarse = {
                mostrarDialogoAuth = false
                controladorNav.navigate(Rutas.Registro.ruta)
            }
        )
    }

    // Diálogo del Chat con Gemini
    if (mostrarChat) {
        Dialog(
            onDismissRequest = { mostrarChat = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                PantallaChat(
                    onDismiss = { mostrarChat = false }
                )
            }
        }
    }
}