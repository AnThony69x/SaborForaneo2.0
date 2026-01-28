package com.example.saborforaneo.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.saborforaneo.ui.components.BarraNavegacionInferiorAdmin
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// Modelo de datos para Usuario en gestión
data class UsuarioAdmin(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val fotoPerfil: String = "",
    val fechaRegistro: Long = 0L,
    val esAdmin: Boolean = false,
    val estaBaneado: Boolean = false,
    val ultimoAcceso: Long = 0L,
    val recetasComunidad: Int = 0
)

data class EstadoGestionUsuarios(
    val usuarios: List<UsuarioAdmin> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val totalUsuarios: Int = 0,
    val totalBaneados: Int = 0,
    val usuariosActivos: Int = 0
)

class GestionUsuariosViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _estado = MutableStateFlow(EstadoGestionUsuarios())
    val estado: StateFlow<EstadoGestionUsuarios> = _estado.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, error = null)

            try {
                val snapshot = firestore.collection("usuarios").get().await()

                // Contar recetas por usuario en comunidad
                val recetasComunidad = firestore.collection("recetas_comunidad").get().await()
                val recetasPorUsuario = recetasComunidad.documents
                    .groupBy { it.getString("autorUid") ?: "" }
                    .mapValues { it.value.size }

                val usuarios = snapshot.documents.mapNotNull { doc ->
                    try {
                        val esAdmin = doc.getBoolean("esAdmin") ?: (doc.getString("rol") == "admin")

                        // Excluir admins de la lista
                        if (esAdmin) return@mapNotNull null

                        val uid = doc.id

                        // Obtener fechaRegistro de diferentes campos posibles
                        val fechaRegistro = doc.getLong("fechaRegistro")
                            ?: doc.getLong("fechaCreacion")
                            ?: doc.getTimestamp("createdAt")?.toDate()?.time
                            ?: 0L

                        // Obtener ultimo acceso
                        val ultimoAcceso = doc.getLong("ultimoAcceso")
                            ?: doc.getLong("lastLogin")
                            ?: doc.getTimestamp("lastLoginAt")?.toDate()?.time
                            ?: 0L

                        UsuarioAdmin(
                            uid = uid,
                            nombre = doc.getString("nombre") ?: "Sin nombre",
                            email = doc.getString("email") ?: "",
                            fotoPerfil = doc.getString("fotoPerfil") ?: "",
                            fechaRegistro = fechaRegistro,
                            esAdmin = false,
                            estaBaneado = doc.getBoolean("estaBaneado") ?: false,
                            ultimoAcceso = ultimoAcceso,
                            recetasComunidad = recetasPorUsuario[uid] ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.fechaRegistro }

                _estado.value = _estado.value.copy(
                    usuarios = usuarios,
                    cargando = false,
                    totalUsuarios = usuarios.size,
                    totalBaneados = usuarios.count { it.estaBaneado },
                    usuariosActivos = usuarios.count { !it.estaBaneado }
                )

            } catch (e: Exception) {
                _estado.value = _estado.value.copy(
                    cargando = false,
                    error = "Error al cargar usuarios: ${e.message}"
                )
            }
        }
    }

    fun banearUsuario(uid: String, banear: Boolean) {
        viewModelScope.launch {
            try {
                firestore.collection("usuarios").document(uid)
                    .update(mapOf(
                        "estaBaneado" to banear,
                        "fechaBaneo" to if (banear) System.currentTimeMillis() else null
                    ))
                    .await()

                // Actualizar lista local
                _estado.value = _estado.value.copy(
                    usuarios = _estado.value.usuarios.map {
                        if (it.uid == uid) it.copy(estaBaneado = banear) else it
                    },
                    totalBaneados = if (banear) _estado.value.totalBaneados + 1 else _estado.value.totalBaneados - 1,
                    usuariosActivos = if (banear) _estado.value.usuariosActivos - 1 else _estado.value.usuariosActivos + 1
                )
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(
                    error = "Error al ${if (banear) "banear" else "desbanear"} usuario: ${e.message}"
                )
            }
        }
    }

    fun eliminarUsuario(uid: String) {
        viewModelScope.launch {
            try {
                // Eliminar documento del usuario
                firestore.collection("usuarios").document(uid).delete().await()

                // Actualizar lista local
                val usuarioEliminado = _estado.value.usuarios.find { it.uid == uid }
                _estado.value = _estado.value.copy(
                    usuarios = _estado.value.usuarios.filter { it.uid != uid },
                    totalUsuarios = _estado.value.totalUsuarios - 1,
                    totalBaneados = if (usuarioEliminado?.estaBaneado == true) _estado.value.totalBaneados - 1 else _estado.value.totalBaneados,
                    usuariosActivos = if (usuarioEliminado?.estaBaneado == false) _estado.value.usuariosActivos - 1 else _estado.value.usuariosActivos
                )
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(
                    error = "Error al eliminar usuario: ${e.message}"
                )
            }
        }
    }

    fun limpiarError() {
        _estado.value = _estado.value.copy(error = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionUsuarios(
    controladorNav: NavController
) {
    val viewModel: GestionUsuariosViewModel = viewModel()
    val estado by viewModel.estado.collectAsState()

    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf("todos") } // todos, baneados, activos
    var mostrarEstadisticas by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar error
    LaunchedEffect(estado.error) {
        estado.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    val usuariosFiltrados = remember(estado.usuarios, busqueda, filtro) {
        estado.usuarios
            .filter {
                if (busqueda.isEmpty()) true
                else it.nombre.contains(busqueda, ignoreCase = true) ||
                     it.email.contains(busqueda, ignoreCase = true)
            }
            .filter {
                when (filtro) {
                    "baneados" -> it.estaBaneado
                    "activos" -> !it.estaBaneado
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
                            "Gestión de Usuarios",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${estado.totalUsuarios} usuarios registrados",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarEstadisticas = !mostrarEstadisticas }) {
                        Icon(
                            imageVector = if (mostrarEstadisticas) Icons.Default.Close else Icons.Default.BarChart,
                            contentDescription = "Estadísticas"
                        )
                    }
                    IconButton(onClick = { viewModel.cargarUsuarios() }) {
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Estadísticas
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
                            "📊 Estadísticas de Usuarios",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            EstadisticaUsuarioCard("👥 Total", estado.totalUsuarios)
                            EstadisticaUsuarioCard("🚫 Baneados", estado.totalBaneados)
                            EstadisticaUsuarioCard("✅ Activos", estado.usuariosActivos)
                        }
                    }
                }
            }

            // Búsqueda y filtros
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar por nombre o email...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtro == "todos",
                        onClick = { filtro = "todos" },
                        label = { Text("Todos") },
                        leadingIcon = if (filtro == "todos") {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = filtro == "baneados",
                        onClick = { filtro = "baneados" },
                        label = { Text("Baneados") },
                        leadingIcon = {
                            Icon(Icons.Default.Block, null, modifier = Modifier.size(18.dp),
                                tint = if (filtro == "baneados") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )
                    FilterChip(
                        selected = filtro == "activos",
                        onClick = { filtro = "activos" },
                        label = { Text("Activos") },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }

            // Lista de usuarios
            when {
                estado.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                usuariosFiltrados.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥", fontSize = 64.sp)
                            Text(
                                text = if (busqueda.isEmpty()) "No hay usuarios" else "No se encontraron usuarios",
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
                            items = usuariosFiltrados,
                            key = { it.uid }
                        ) { usuario ->
                            TarjetaUsuario(
                                usuario = usuario,
                                onBanear = { viewModel.banearUsuario(usuario.uid, !usuario.estaBaneado) },
                                onEliminar = { viewModel.eliminarUsuario(usuario.uid) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadisticaUsuarioCard(titulo: String, valor: Int) {
    Card(modifier = Modifier.width(80.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TarjetaUsuario(
    usuario: UsuarioAdmin,
    onBanear: () -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarDialogoBanear by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    val fechaRegistro = remember(usuario.fechaRegistro) {
        if (usuario.fechaRegistro > 0) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(usuario.fechaRegistro))
        } else "Sin datos"
    }

    val ultimoAcceso = remember(usuario.ultimoAcceso) {
        if (usuario.ultimoAcceso > 0) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(usuario.ultimoAcceso))
        } else "Sin datos"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (usuario.estaBaneado)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con foto y nombre
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto de perfil
                if (usuario.fotoPerfil.isNotEmpty()) {
                    AsyncImage(
                        model = usuario.fotoPerfil,
                        contentDescription = "Foto de ${usuario.nombre}",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = usuario.nombre.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = usuario.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (usuario.estaBaneado) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = "BANEADO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = usuario.email,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider()

            // Información del usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "📅 Registro",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = fechaRegistro,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📝 Recetas",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = usuario.recetasComunidad.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "🕐 Último acceso",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ultimoAcceso,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Acciones rápidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { mostrarDialogoBanear = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (usuario.estaBaneado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        if (usuario.estaBaneado) Icons.Default.CheckCircle else Icons.Default.Block,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (usuario.estaBaneado) "Desbanear" else "Banear")
                }

                OutlinedButton(
                    onClick = { mostrarDialogoEliminar = true },
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
        }
    }

    // Diálogo de banear/desbanear
    if (mostrarDialogoBanear) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBanear = false },
            icon = {
                Icon(
                    imageVector = if (usuario.estaBaneado) Icons.Default.CheckCircle else Icons.Default.Block,
                    contentDescription = null,
                    tint = if (usuario.estaBaneado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(if (usuario.estaBaneado) "¿Desbanear usuario?" else "¿Banear usuario?")
            },
            text = {
                Text(
                    if (usuario.estaBaneado)
                        "El usuario \"${usuario.nombre}\" podrá volver a acceder a la aplicación."
                    else
                        "El usuario \"${usuario.nombre}\" no podrá acceder a la aplicación hasta que sea desbaneado."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBanear()
                        mostrarDialogoBanear = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (usuario.estaBaneado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (usuario.estaBaneado) "Desbanear" else "Banear")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBanear = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de eliminar
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("¿Eliminar usuario?") },
            text = {
                Text("Esta acción eliminará permanentemente al usuario \"${usuario.nombre}\" y todos sus datos. No se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEliminar()
                        mostrarDialogoEliminar = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
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
