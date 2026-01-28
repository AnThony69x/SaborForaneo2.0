package com.example.saborforaneo.ui.screens.admin

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.saborforaneo.data.model.RecetaComunidad
import com.example.saborforaneo.ui.components.BarraNavegacionInferiorAdmin
import com.example.saborforaneo.viewmodel.GestionComunidadViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionComunidad(
    controladorNav: NavController
) {
    val context = LocalContext.current
    val viewModel: GestionComunidadViewModel = viewModel {
        GestionComunidadViewModel(context.applicationContext as Application)
    }
    val recetas by viewModel.recetas.collectAsState()
    val estadisticas by viewModel.estadisticas.collectAsState()
    
    var busqueda by remember { mutableStateOf("") }
    var filtroEstado by remember { mutableStateOf("todas") } // todas, publicadas, pendientes, rechazadas
    var mostrarEstadisticas by remember { mutableStateOf(false) }

    // Cargar recetas al entrar
    LaunchedEffect(Unit) {
        viewModel.cargarRecetasComunidad()
    }

    val recetasFiltradas = remember(recetas.recetas, busqueda, filtroEstado) {
        recetas.recetas
            .filter {
                if (busqueda.isEmpty()) true
                else it.nombre.contains(busqueda, ignoreCase = true) ||
                     it.nombreAutor.contains(busqueda, ignoreCase = true)
            }
            .filter {
                when (filtroEstado) {
                    "publicadas" -> it.publicada
                    "pendientes" -> !it.publicada && !it.rechazada
                    "rechazadas" -> it.rechazada
                    else -> true
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { controladorNav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            "Gestión de Comunidad",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${recetas.recetas.size} recetas • ${estadisticas.totalPublicadas} publicadas",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarEstadisticas = !mostrarEstadisticas }) {
                        Icon(
                            imageVector = if (mostrarEstadisticas) Icons.Default.Close else Icons.Default.BarChart,
                            contentDescription = if (mostrarEstadisticas) "Ocultar estadísticas" else "Ver estadísticas"
                        )
                    }
                    IconButton(onClick = {
                        viewModel.arreglarRecetasPublicadas()
                        viewModel.cargarRecetasComunidad()
                    }) {
                        Icon(Icons.Default.Build, "Arreglar recetas")
                    }
                    IconButton(onClick = { viewModel.cargarRecetasComunidad() }) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
        ) {
            // Estadísticas (si están visibles)
            AnimatedVisibility(
                visible = mostrarEstadisticas,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "📊 Estadísticas de Comunidad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            EstadisticaCard("✅ Publicadas", estadisticas.totalPublicadas)
                            EstadisticaCard("⏳ Pendientes", estadisticas.totalPendientes)
                            EstadisticaCard("❌ Rechazadas", estadisticas.totalRechazadas)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            EstadisticaCard("❤️ Favoritos", estadisticas.totalFavoritos)
                            EstadisticaCard("👥 Autores", estadisticas.autoresActivos)
                            EstadisticaCard("📅 Hoy", estadisticas.recetasHoy)
                        }
                    }
                }
            }

            // Barra de búsqueda y filtros
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar por nombre o autor...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Buscar")
                    },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(Icons.Default.Close, "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Filtros por estado
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = filtroEstado == "todas",
                        onClick = { filtroEstado = "todas" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
                    ) {
                        Text("Todas", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = filtroEstado == "publicadas",
                        onClick = { filtroEstado = "publicadas" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
                    ) {
                        Text("✓", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = filtroEstado == "pendientes",
                        onClick = { filtroEstado = "pendientes" },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
                    ) {
                        Text("⏳", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = filtroEstado == "rechazadas",
                        onClick = { filtroEstado = "rechazadas" },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4)
                    ) {
                        Text("✗", fontSize = 12.sp)
                    }
                }
            }
            when {
                recetas.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                recetas.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error al cargar recetas",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = recetas.error ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.cargarRecetasComunidad() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                recetasFiltradas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🍽️",
                                fontSize = 64.sp
                            )
                            Text(
                                text = if (busqueda.isEmpty()) "No hay recetas" else "No se encontraron recetas",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = recetasFiltradas,
                            key = { it.id }
                        ) { receta ->
                            TarjetaRecetaComunidad(
                                receta = receta,
                                onPublicar = { viewModel.publicarReceta(it) },
                                onRechazar = { viewModel.rechazarReceta(it) },
                                onEliminar = { viewModel.eliminarReceta(it) },
                                onRestaurar = { viewModel.restaurarReceta(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadisticaCard(
    titulo: String,
    valor: Int
) {
    Card(
        modifier = Modifier.width(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TarjetaRecetaComunidad(
    receta: RecetaComunidad,
    onPublicar: (String) -> Unit,
    onRechazar: (String) -> Unit,
    onEliminar: (String) -> Unit,
    onRestaurar: (String) -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var accionSeleccionada by remember { mutableStateOf<String?>(null) }
    
    val fechaFormato = remember(receta.fechaCreacion) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(receta.fechaCreacion))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Imagen de la receta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = receta.imagenUrl,
                    contentDescription = receta.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Badge de estado
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = when {
                        receta.publicada -> MaterialTheme.colorScheme.tertiary
                        receta.rechazada -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text(
                        text = when {
                            receta.publicada -> "✅ PUBLICADA"
                            receta.rechazada -> "❌ RECHAZADA"
                            else -> "⏳ PENDIENTE"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // Contador de favoritos
                if (receta.totalFavoritos > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = receta.totalFavoritos.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Información de la receta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = receta.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = receta.descripcion,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Divider()
                
                // Info del autor y fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = receta.nombreAutor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = fechaFormato,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    IconButton(onClick = { mostrarDialogo = true }) {
                        Icon(Icons.Default.MoreVert, "Opciones")
                    }
                }
                
                // Información adicional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoChip(Icons.Default.Timer, "${receta.tiempoPreparacion} min")
                    InfoChip(Icons.Default.Restaurant, "${receta.porciones} porciones")
                    InfoChip(Icons.Default.Category, receta.categoria)
                }
                
                // Botones de acción rápida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when {
                        !receta.publicada && !receta.rechazada -> {
                            // Receta pendiente - mostrar publicar y rechazar
                            Button(
                                onClick = { 
                                    accionSeleccionada = "publicar"
                                    mostrarDialogo = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Publicar")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    accionSeleccionada = "rechazar"
                                    mostrarDialogo = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rechazar")
                            }
                        }
                        receta.rechazada -> {
                            // Receta rechazada - mostrar restaurar y eliminar
                            Button(
                                onClick = {
                                    accionSeleccionada = "restaurar"
                                    mostrarDialogo = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restaurar")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    accionSeleccionada = "eliminar"
                                    mostrarDialogo = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eliminar")
                            }
                        }
                        receta.publicada -> {
                            // Receta publicada - mostrar solo eliminar
                            OutlinedButton(
                                onClick = {
                                    accionSeleccionada = "eliminar"
                                    mostrarDialogo = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eliminar Receta")
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (mostrarDialogo && accionSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { 
                mostrarDialogo = false
                accionSeleccionada = null
            },
            icon = {
                Icon(
                    imageVector = when (accionSeleccionada) {
                        "publicar" -> Icons.Default.CheckCircle
                        "rechazar" -> Icons.Default.Close
                        "eliminar" -> Icons.Default.Delete
                        "restaurar" -> Icons.Default.RestartAlt
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when (accionSeleccionada) {
                        "eliminar", "rechazar" -> MaterialTheme.colorScheme.error
                        "publicar" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            },
            title = {
                Text(
                    text = when (accionSeleccionada) {
                        "publicar" -> "¿Publicar receta?"
                        "rechazar" -> "¿Rechazar receta?"
                        "eliminar" -> "¿Eliminar receta?"
                        "restaurar" -> "¿Restaurar receta?"
                        else -> "Confirmar acción"
                    }
                )
            },
            text = {
                Text(
                    text = when (accionSeleccionada) {
                        "publicar" -> "La receta \"${receta.nombre}\" será visible para todos los usuarios en la comunidad."
                        "rechazar" -> "La receta \"${receta.nombre}\" será marcada como rechazada y no será visible en la comunidad."
                        "eliminar" -> "Esta acción eliminará permanentemente la receta \"${receta.nombre}\". No se puede deshacer."
                        "restaurar" -> "La receta \"${receta.nombre}\" volverá al estado pendiente de revisión."
                        else -> ""
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (accionSeleccionada) {
                            "publicar" -> onPublicar(receta.id)
                            "rechazar" -> onRechazar(receta.id)
                            "eliminar" -> onEliminar(receta.id)
                            "restaurar" -> onRestaurar(receta.id)
                        }
                        mostrarDialogo = false
                        accionSeleccionada = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (accionSeleccionada) {
                            "eliminar", "rechazar" -> MaterialTheme.colorScheme.error
                            "publicar" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    Text(
                        when (accionSeleccionada) {
                            "publicar" -> "Sí, publicar"
                            "rechazar" -> "Sí, rechazar"
                            "eliminar" -> "Sí, eliminar"
                            "restaurar" -> "Sí, restaurar"
                            else -> "Confirmar"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                        accionSeleccionada = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    } else if (mostrarDialogo) {
        // Diálogo de detalles
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Detalles de la Receta") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📝 ${receta.nombre}", fontWeight = FontWeight.Bold)
                    Text("👤 Autor: ${receta.nombreAutor}")
                    Text("🆔 ID: ${receta.id.take(20)}...")
                    Text("📅 Creada: $fechaFormato")
                    Text("❤️ Favoritos: ${receta.totalFavoritos}")
                    Text("🍽️ Categoría: ${receta.categoria}")
                    Text("⏱️ Tiempo: ${receta.tiempoPreparacion} min")
                    Text("👥 Porciones: ${receta.porciones}")
                    Text("📊 Dificultad: ${receta.dificultad}")
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun InfoChip(
    icono: ImageVector,
    texto: String
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = texto,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}