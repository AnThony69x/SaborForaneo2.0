package com.example.saborforaneo.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.example.saborforaneo.data.model.EstadoModeracion
import com.example.saborforaneo.data.model.Receta
import com.example.saborforaneo.ui.components.BarraNavegacionInferiorAdmin
import com.example.saborforaneo.viewmodel.ModeracionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaModeracion(
    controladorNav: NavController,
    authViewModel: com.example.saborforaneo.viewmodel.AuthViewModel
) {
    val context = LocalContext.current
    val viewModel = remember { ModeracionViewModel(context) }
    val recetasPendientes by viewModel.recetasPendientes.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var recetaSeleccionada by remember { mutableStateOf<Receta?>(null) }
    var mostrarDialogoRechazo by remember { mutableStateOf(false) }
    var motivoRechazo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarRecetasPendientes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Moderación de Recetas",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${recetasPendientes.size} pendientes de revisión",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarRecetasPendientes() }) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BarraNavegacionInferiorAdmin(controladorNav = controladorNav)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        
        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Cargando recetas...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else if (recetasPendientes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "¡Todo al día!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "No hay recetas pendientes de moderación",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recetasPendientes, key = { it.id }) { receta ->
                    TarjetaRecetaPendiente(
                        receta = receta,
                        onAprobar = {
                            scope.launch {
                                val exito = viewModel.aprobarReceta(
                                    receta.id,
                                    authViewModel.currentUser.value?.uid ?: ""
                                )
                                snackbarHostState.showSnackbar(
                                    if (exito) "✅ Receta aprobada" else "❌ Error al aprobar"
                                )
                            }
                        },
                        onRechazar = {
                            recetaSeleccionada = receta
                            mostrarDialogoRechazo = true
                        }
                    )
                }
            }
        }
    }

    // Diálogo de rechazo
    if (mostrarDialogoRechazo && recetaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { 
                mostrarDialogoRechazo = false
                motivoRechazo = ""
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Rechazar Receta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("¿Estás seguro de rechazar esta receta?")
                    Text(
                        text = recetaSeleccionada?.nombre ?: "",
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = motivoRechazo,
                        onValueChange = { motivoRechazo = it },
                        label = { Text("Motivo del rechazo") },
                        placeholder = { Text("Ej: Contenido inapropiado, mala calidad...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            val exito = viewModel.rechazarReceta(
                                recetaSeleccionada?.id ?: "",
                                authViewModel.currentUser.value?.uid ?: "",
                                motivoRechazo
                            )
                            snackbarHostState.showSnackbar(
                                if (exito) "❌ Receta rechazada" else "❌ Error al rechazar"
                            )
                            mostrarDialogoRechazo = false
                            motivoRechazo = ""
                        }
                    },
                    enabled = motivoRechazo.isNotBlank(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    mostrarDialogoRechazo = false
                    motivoRechazo = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TarjetaRecetaPendiente(
    receta: Receta,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Imagen
            if (receta.imagenUrl.isNotEmpty()) {
                AsyncImage(
                    model = receta.imagenUrl,
                    contentDescription = receta.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Título y badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = receta.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    AssistChip(
                        onClick = { },
                        label = { Text("Pendiente") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }

                // Descripción
                Text(
                    text = receta.descripcion,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3
                )

                HorizontalDivider()

                // Información
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChipModeracion(
                        icon = Icons.Default.Person,
                        text = receta.creadoPor
                    )
                    InfoChipModeracion(
                        icon = Icons.Default.DateRange,
                        text = dateFormat.format(Date(receta.fechaCreacion))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChipModeracion(
                        icon = Icons.Default.Category,
                        text = receta.categoria
                    )
                    InfoChipModeracion(
                        icon = Icons.Default.Timer,
                        text = "${receta.tiempoPreparacion} min"
                    )
                }

                HorizontalDivider()

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onRechazar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Rechazar")
                    }

                    Button(
                        onClick = onAprobar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Aprobar")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChipModeracion(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
