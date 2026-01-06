package com.example.saborforaneo.ui.screens.community

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.saborforaneo.data.model.RecetaComunidad
import com.example.saborforaneo.ui.components.BarraNavegacionInferior
import com.example.saborforaneo.viewmodel.ComunidadViewModel
import com.example.saborforaneo.viewmodel.VistaComunidad
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaComunidad(
    navegarAtras: () -> Unit,
    navegarACrearReceta: () -> Unit,
    navegarADetalle: (String, Boolean) -> Unit, // Añadido parámetro scrollToComments
    controladorNav: NavController,
    viewModel: ComunidadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunidad SaborForáneo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = navegarAtras) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.cambiarVista(
                            if (uiState.vistaActual == VistaComunidad.TODAS)
                                VistaComunidad.MIS_RECETAS
                            else
                                VistaComunidad.TODAS
                        )
                    }) {
                        Icon(
                            imageVector = if (uiState.vistaActual == VistaComunidad.TODAS)
                                Icons.Default.Person
                            else
                                Icons.Default.Public,
                            contentDescription = "Cambiar vista"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(controladorNav = controladorNav)
        },
        floatingActionButton = {
            if (uiState.vistaActual == VistaComunidad.MIS_RECETAS) {
                FloatingActionButton(
                    onClick = navegarACrearReceta,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Crear receta")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = if (uiState.vistaActual == VistaComunidad.TODAS) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.vistaActual == VistaComunidad.TODAS,
                    onClick = { viewModel.cambiarVista(VistaComunidad.TODAS) },
                    text = { Text("🌎 Todas") }
                )
                Tab(
                    selected = uiState.vistaActual == VistaComunidad.MIS_RECETAS,
                    onClick = { viewModel.cambiarVista(VistaComunidad.MIS_RECETAS) },
                    text = { Text("📝 Mis Recetas") }
                )
            }

            // Contenido
            if (uiState.cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val recetasMostrar = if (uiState.vistaActual == VistaComunidad.TODAS)
                    uiState.recetas
                else
                    uiState.misRecetas

                if (recetasMostrar.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = if (uiState.vistaActual == VistaComunidad.TODAS) "🍽️" else "📝",
                                fontSize = 64.sp
                            )
                            Text(
                                text = if (uiState.vistaActual == VistaComunidad.TODAS)
                                    "No hay recetas en la comunidad"
                                else
                                    "No tienes recetas creadas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (uiState.vistaActual == VistaComunidad.MIS_RECETAS) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = navegarACrearReceta) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Crear mi primera receta")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = recetasMostrar,
                            key = { it.id }
                        ) { receta ->
                            TarjetaRecetaComunidad(
                                receta = receta,
                                onLikeClick = { viewModel.toggleLike(receta.id) },
                                onVerDetalle = { navegarADetalle(receta.id, false) },
                                onVerComentarios = { navegarADetalle(receta.id, true) },
                                onEditClick = if (uiState.vistaActual == VistaComunidad.MIS_RECETAS) {
                                    { /* TODO: Navegar a editar */ }
                                } else null,
                                onDeleteClick = if (uiState.vistaActual == VistaComunidad.MIS_RECETAS) {
                                    {
                                        scope.launch {
                                            viewModel.eliminarReceta(
                                                receta.id,
                                                onSuccess = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Receta eliminada")
                                                    }
                                                },
                                                onError = { error ->
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(error)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaRecetaComunidad(
    receta: RecetaComunidad,
    onLikeClick: () -> Unit,
    onVerDetalle: () -> Unit,
    onVerComentarios: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val leDioLike = receta.usuariosQueLikean.contains(currentUserId)
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Imagen de la receta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (receta.imagenUrl.isNotEmpty()) {
                    AsyncImage(
                        model = receta.imagenUrl,
                        contentDescription = receta.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }

                // Botones de acción (para recetas propias)
                if (onEditClick != null || onDeleteClick != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        onEditClick?.let {
                            FloatingActionButton(
                                onClick = it,
                                modifier = Modifier.size(40.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    "Editar",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        onDeleteClick?.let {
                            FloatingActionButton(
                                onClick = { mostrarDialogoEliminar = true },
                                modifier = Modifier.size(40.dp),
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    "Eliminar",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Información del autor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    if (receta.autorFoto.isNotEmpty()) {
                        AsyncImage(
                            model = receta.autorFoto,
                            contentDescription = receta.autorNombre,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = receta.autorNombre.firstOrNull()?.toString()?.uppercase() ?: "?",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = receta.autorNombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatearFecha(receta.fechaCreacion),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Título de la receta
                Text(
                    text = receta.nombre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = receta.descripcion,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Información de la receta
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Chip(text = "⏱️ ${receta.tiempoPreparacion} min")
                    Chip(text = "🍽️ ${receta.porciones} porciones")
                    Chip(text = receta.categoria)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Acciones (like, comentarios)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Botón de like
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onLikeClick)
                        ) {
                            Icon(
                                imageVector = if (leDioLike) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (leDioLike) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = receta.likes.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Comentarios - clickeable para ir a la sección de comentarios
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                onVerComentarios() // Navegar al detalle y hacer scroll a comentarios
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Comment,
                                contentDescription = "Comentarios",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = receta.comentarios.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Botón ver más
                    TextButton(onClick = onVerDetalle) {
                        Text("Ver receta")
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar receta") },
            text = { Text("¿Estás seguro de que quieres eliminar esta receta? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEliminar = false
                        onDeleteClick?.invoke()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

fun formatearFecha(timestamp: Long): String {
    val ahora = System.currentTimeMillis()
    val diferencia = ahora - timestamp

    return when {
        diferencia < 60000 -> "Hace un momento"
        diferencia < 3600000 -> "Hace ${diferencia / 60000} min"
        diferencia < 86400000 -> "Hace ${diferencia / 3600000} h"
        diferencia < 604800000 -> "Hace ${diferencia / 86400000} días"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

