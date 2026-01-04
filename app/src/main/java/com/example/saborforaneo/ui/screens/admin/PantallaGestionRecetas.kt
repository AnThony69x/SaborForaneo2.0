package com.example.saborforaneo.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.saborforaneo.data.model.Receta
import com.example.saborforaneo.ui.components.BarraNavegacionInferiorAdmin
import com.example.saborforaneo.viewmodel.RecetaAdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionRecetas(
    viewModel: RecetaAdminViewModel,
    userId: String,
    controladorNav: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf<Receta?>(null) }

    // Mostrar snackbar de éxito o error
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.operacionExitosa, uiState.error) {
        if (uiState.operacionExitosa && uiState.mensajeExito != null) {
            snackbarHostState.showSnackbar(
                message = uiState.mensajeExito!!,
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarEstadoOperacion()
        }

        if (uiState.error != null) {
            snackbarHostState.showSnackbar(
                message = uiState.error!!,
                duration = SnackbarDuration.Long
            )
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gestión de Recetas",
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { mostrarDialogoAgregar = true },
                icon = { Icon(Icons.Default.Add, "Agregar") },
                text = { Text("Nueva Receta") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Encabezado con información
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recetas Personalizadas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.recetas.size} receta(s) agregada(s)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { viewModel.cargarRecetasAdmin() }) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                }
            }

            // Lista de recetas
            if (uiState.cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.recetas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "No hay recetas personalizadas",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Las recetas predefinidas no aparecen aquí",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.recetas) { receta ->
                        TarjetaRecetaAdmin(
                            receta = receta,
                            onEditar = {
                                viewModel.seleccionarReceta(receta)
                                mostrarDialogoEditar = true
                            },
                            onEliminar = { mostrarDialogoEliminar = receta }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para agregar receta
    if (mostrarDialogoAgregar) {
        DialogoFormularioReceta(
            titulo = "Nueva Receta",
            receta = null,
            onDismiss = { mostrarDialogoAgregar = false },
            onGuardar = { receta ->
                viewModel.agregarReceta(receta, userId)
                mostrarDialogoAgregar = false
            }
        )
    }

    // Diálogo para editar receta
    if (mostrarDialogoEditar && uiState.recetaSeleccionada != null) {
        DialogoFormularioReceta(
            titulo = "Editar Receta",
            receta = uiState.recetaSeleccionada,
            onDismiss = {
                mostrarDialogoEditar = false
                viewModel.seleccionarReceta(null)
            },
            onGuardar = { receta ->
                viewModel.actualizarReceta(uiState.recetaSeleccionada!!.id, receta)
                mostrarDialogoEditar = false
                viewModel.seleccionarReceta(null)
            }
        )
    }

    // Diálogo de confirmación para eliminar
    if (mostrarDialogoEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = null },
            icon = { Icon(Icons.Default.Delete, null) },
            title = { Text("Eliminar Receta") },
            text = { Text("¿Estás seguro de que deseas eliminar \"${mostrarDialogoEliminar!!.nombre}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarReceta(mostrarDialogoEliminar!!.id)
                        mostrarDialogoEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaRecetaAdmin(
    receta: Receta,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen
            AsyncImage(
                model = receta.imagenUrl,
                contentDescription = receta.nombre,
                modifier = Modifier.size(80.dp)
            )

            // Información
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = receta.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = receta.categoria,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${receta.tiempoPreparacion} min • ${receta.dificultad.name}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Acciones
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEditar,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

