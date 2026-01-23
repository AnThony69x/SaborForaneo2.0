package com.example.saborforaneo.ui.screens.favorites

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saborforaneo.ui.components.BarraNavegacionInferior
import com.example.saborforaneo.ui.components.DialogoRequiereAuth
import com.example.saborforaneo.ui.components.MensajesAuth
import com.example.saborforaneo.ui.components.TarjetaReceta
import com.example.saborforaneo.ui.navigation.Rutas
import com.example.saborforaneo.viewmodel.FavoritosViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFavoritos(
    navegarADetalle: (String) -> Unit,
    controladorNav: NavController
) {
    val contexto = LocalContext.current
    val usuarioActual = FirebaseAuth.getInstance().currentUser
    val estaAutenticado = usuarioActual != null

    // Estado para mostrar diálogo de autenticación
    var mostrarDialogoAuth by remember { mutableStateOf(false) }

    // Solo crear el ViewModel si está autenticado
    val viewModel = remember(estaAutenticado) {
        if (estaAutenticado) FavoritosViewModel(contexto) else null
    }
    val uiState = viewModel?.uiState?.collectAsState()

    // Mostrar diálogo automáticamente si no está autenticado
    LaunchedEffect(estaAutenticado) {
        if (!estaAutenticado) {
            mostrarDialogoAuth = true
        }
    }

    // Recargar favoritos cada vez que se navega a esta pantalla
    LaunchedEffect(estaAutenticado) {
        if (estaAutenticado) {
            viewModel?.cargarFavoritos()
        }
    }

    // Diálogo de autenticación requerida
    if (mostrarDialogoAuth && !estaAutenticado) {
        DialogoRequiereAuth(
            titulo = MensajesAuth.FAVORITOS.first,
            mensaje = MensajesAuth.FAVORITOS.second,
            emoji = MensajesAuth.FAVORITOS.third,
            onDismiss = {
                mostrarDialogoAuth = false
                // Navegar de regreso a inicio
                controladorNav.navigate(Rutas.Inicio.ruta) {
                    popUpTo(Rutas.Favoritos.ruta) { inclusive = true }
                }
            },
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mis Favoritos",
                            fontWeight = FontWeight.Bold
                        )
                        if (estaAutenticado) {
                            Text(
                                text = "${uiState?.value?.recetasFavoritas?.size ?: 0} recetas guardadas",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(controladorNav = controladorNav)
        }
    ) { paddingValues ->
        // Si no está autenticado, mostrar mensaje
        if (!estaAutenticado) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "❤️",
                        fontSize = 72.sp
                    )
                    Text(
                        text = "Guarda tus recetas favoritas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Crea una cuenta para guardar tus recetas favoritas y acceder a ellas en cualquier momento",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { mostrarDialogoAuth = true },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("Crear cuenta")
                    }
                    OutlinedButton(
                        onClick = { controladorNav.navigate(Rutas.Login.ruta) },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("Iniciar sesión")
                    }
                }
            }
        } else if (uiState?.value?.cargando == true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState?.value?.error != null) {
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
                        text = "Error al cargar favoritos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.value.error ?: "Error desconocido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Button(onClick = { viewModel?.cargarFavoritos() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else if (uiState?.value?.recetasFavoritas?.isEmpty() == true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "❤️",
                        fontSize = 72.sp
                    )
                    Text(
                        text = "No tienes favoritos aún",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Explora recetas y guarda tus favoritas",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = uiState?.value?.recetasFavoritas ?: emptyList(),
                    key = { _, receta -> receta.id }
                ) { index, receta ->
                    var visible by remember { mutableStateOf(false) }

                    LaunchedEffect(key1 = receta.id) {
                        delay(index * 50L)
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    ) {
                        TarjetaReceta(
                            receta = receta,
                            alHacerClick = { navegarADetalle(receta.id) },
                            esFavorito = true,
                            onToggleFavorito = {
                                viewModel?.toggleFavorito(receta.id)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

