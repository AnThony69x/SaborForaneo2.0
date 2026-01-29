package com.example.saborforaneo.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
    val tipoBaneo: String = "", // "temporal" o "permanente"
    val motivoBaneo: String = "",
    val fechaBaneo: Long? = null,
    val fechaFinBaneo: Long? = null, // Solo para baneos temporales
    val cuentaEliminada: Boolean = false,
    val fechaEliminacion: Long? = null,
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

                        // Excluir admins y cuentas eliminadas de la lista
                        if (esAdmin) return@mapNotNull null
                        
                        val cuentaEliminada = doc.getBoolean("cuentaEliminada") ?: false
                        if (cuentaEliminada) return@mapNotNull null

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
                            tipoBaneo = doc.getString("tipoBaneo") ?: "",
                            motivoBaneo = doc.getString("motivoBaneo") ?: "",
                            fechaBaneo = doc.getLong("fechaBaneo") ?: 0L,
                            fechaFinBaneo = doc.getLong("fechaFinBaneo") ?: 0L,
                            cuentaEliminada = doc.getBoolean("cuentaEliminada") ?: false,
                            fechaEliminacion = doc.getLong("fechaEliminacion") ?: 0L,
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

    fun banearUsuario(uid: String, banear: Boolean, tipoBaneo: String = "permanente", motivoBaneo: String = "", fechaFinBaneo: Long = 0L) {
        viewModelScope.launch {
            try {
                val updates = mutableMapOf<String, Any?>(
                    "estaBaneado" to banear,
                    "fechaBaneo" to if (banear) System.currentTimeMillis() else null
                )
                
                if (banear) {
                    updates["tipoBaneo"] = tipoBaneo
                    updates["motivoBaneo"] = motivoBaneo
                    if (tipoBaneo == "temporal") {
                        updates["fechaFinBaneo"] = fechaFinBaneo
                    }
                } else {
                    // Al desbanear, limpiar todos los campos relacionados
                    updates["tipoBaneo"] = ""
                    updates["motivoBaneo"] = ""
                    updates["fechaFinBaneo"] = null
                }
                
                firestore.collection("usuarios").document(uid)
                    .update(updates)
                    .await()

                // Actualizar lista local
                _estado.value = _estado.value.copy(
                    usuarios = _estado.value.usuarios.map {
                        if (it.uid == uid) {
                            it.copy(
                                estaBaneado = banear,
                                tipoBaneo = if (banear) tipoBaneo else "",
                                motivoBaneo = if (banear) motivoBaneo else "",
                                fechaBaneo = if (banear) System.currentTimeMillis() else null,
                                fechaFinBaneo = if (banear && tipoBaneo == "temporal") fechaFinBaneo else null
                            )
                        } else it
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
                // Marcar como eliminado en lugar de borrar completamente
                firestore.collection("usuarios").document(uid)
                    .update(mapOf(
                        "cuentaEliminada" to true,
                        "fechaEliminacion" to System.currentTimeMillis(),
                        "estaBaneado" to false // Limpiar estado de baneo
                    ))
                    .await()

                // Actualizar lista local - eliminar de la vista
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
                                onBanear = { tipoBaneo, motivoBaneo, fechaFinBaneo ->
                                    if (usuario.estaBaneado) {
                                        // Desbanear
                                        viewModel.banearUsuario(usuario.uid, false)
                                    } else {
                                        // Banear
                                        viewModel.banearUsuario(usuario.uid, true, tipoBaneo, motivoBaneo, fechaFinBaneo)
                                    }
                                },
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
    onBanear: (tipoBaneo: String, motivoBaneo: String, fechaFinBaneo: Long) -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarDialogoBanear by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarConfirmacionFinal by remember { mutableStateOf(false) }

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
        if (usuario.estaBaneado) {
            // Diálogo simple para desbanear
            AlertDialog(
                onDismissRequest = { mostrarDialogoBanear = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { Text("¿Desbanear usuario?") },
                text = {
                    Text("El usuario \"${usuario.nombre}\" podrá volver a acceder a la aplicación.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onBanear("", "", 0L)
                            mostrarDialogoBanear = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Desbanear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoBanear = false }) {
                        Text("Cancelar")
                    }
                }
            )
        } else {
            // Diálogo avanzado para banear con opciones
            DialogoBanearUsuario(
                usuario = usuario,
                onConfirm = { tipoBaneo, motivoBaneo, fechaFinBaneo ->
                    onBanear(tipoBaneo, motivoBaneo, fechaFinBaneo)
                    mostrarDialogoBanear = false
                },
                onDismiss = { mostrarDialogoBanear = false }
            )
        }
    }

    // Diálogo de eliminar - Primera confirmación
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Esta acción eliminará permanentemente al usuario:",
                        fontWeight = FontWeight.Medium
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = "${usuario.nombre}\n${usuario.email}",
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "⚠️ Esta acción NO se puede deshacer.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoEliminar = false
                        mostrarConfirmacionFinal = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de confirmación final
    if (mostrarConfirmacionFinal) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionFinal = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { 
                Text(
                    "⚠️ Confirmación Final",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "¿Estás COMPLETAMENTE SEGURO?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "El usuario \"${usuario.nombre}\" será eliminado permanentemente y no podrá acceder nunca más a la aplicación.",
                        fontSize = 14.sp
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🗑️ Esta acción es IRREVERSIBLE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "No hay forma de recuperar la cuenta después",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEliminar()
                        mostrarConfirmacionFinal = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SÍ, ELIMINAR PERMANENTEMENTE")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmacionFinal = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("No, Cancelar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoBanearUsuario(
    usuario: UsuarioAdmin,
    onConfirm: (tipoBaneo: String, motivoBaneo: String, fechaFinBaneo: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var tipoBaneo by remember { mutableStateOf("permanente") } // "temporal" o "permanente"
    var motivoBaneo by remember { mutableStateOf("") }
    var diasBaneo by remember { mutableStateOf("7") }
    
    val motivosPredefinidos = listOf(
        "Violación de términos de servicio",
        "Contenido inapropiado",
        "Spam o publicidad no deseada",
        "Acoso o comportamiento abusivo",
        "Suplantación de identidad",
        "Otro (especificar abajo)"
    )
    
    var motivoSeleccionado by remember { mutableStateOf(motivosPredefinidos[0]) }
    var mostrarCampoPersonalizado by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Banear Usuario",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "Usuario: ${usuario.nombre}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                HorizontalDivider()
                
                // Tipo de baneo
                Text(
                    text = "Tipo de suspensión",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tipoBaneo = "temporal" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipoBaneo == "temporal",
                            onClick = { tipoBaneo = "temporal" }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("⏱️ Temporal", fontWeight = FontWeight.Medium)
                            Text(
                                "El usuario será desbaneado automáticamente",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tipoBaneo = "permanente" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipoBaneo == "permanente",
                            onClick = { tipoBaneo = "permanente" }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("🔒 Permanente", fontWeight = FontWeight.Medium)
                            Text(
                                "Requiere desbaneo manual del administrador",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                // Duración (solo para temporal)
                androidx.compose.animation.AnimatedVisibility(visible = tipoBaneo == "temporal") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Duración del baneo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        OutlinedTextField(
                            value = diasBaneo,
                            onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) diasBaneo = it },
                            label = { Text("Días") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            supportingText = {
                                val dias = diasBaneo.toIntOrNull() ?: 0
                                if (dias > 0) {
                                    val fechaFin = System.currentTimeMillis() + (dias * 24 * 60 * 60 * 1000L)
                                    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    Text("Expirará el: ${formato.format(Date(fechaFin))}")
                                }
                            }
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Motivo del baneo
                Text(
                    text = "Motivo de la suspensión",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    motivosPredefinidos.forEach { motivo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    motivoSeleccionado = motivo
                                    mostrarCampoPersonalizado = motivo.startsWith("Otro")
                                    if (!mostrarCampoPersonalizado) {
                                        motivoBaneo = motivo
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = motivoSeleccionado == motivo,
                                onClick = {
                                    motivoSeleccionado = motivo
                                    mostrarCampoPersonalizado = motivo.startsWith("Otro")
                                    if (!mostrarCampoPersonalizado) {
                                        motivoBaneo = motivo
                                    }
                                }
                            )
                            Text(
                                text = motivo,
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                // Campo personalizado
                androidx.compose.animation.AnimatedVisibility(visible = mostrarCampoPersonalizado) {
                    OutlinedTextField(
                        value = motivoBaneo,
                        onValueChange = { motivoBaneo = it },
                        label = { Text("Especifica el motivo") },
                        placeholder = { Text("Describe el motivo del baneo...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
                
                // Advertencia
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "El usuario será expulsado inmediatamente y verá el motivo del baneo al intentar iniciar sesión.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            val motivoFinal = if (motivoBaneo.isBlank() && !mostrarCampoPersonalizado) {
                                motivoSeleccionado
                            } else {
                                motivoBaneo.ifBlank { "Sin motivo especificado" }
                            }
                            
                            val fechaFin = if (tipoBaneo == "temporal") {
                                val dias = diasBaneo.toIntOrNull() ?: 7
                                System.currentTimeMillis() + (dias * 24 * 60 * 60 * 1000L)
                            } else {
                                0L
                            }
                            
                            onConfirm(tipoBaneo, motivoFinal, fechaFin)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = if (tipoBaneo == "temporal") diasBaneo.toIntOrNull() != null && diasBaneo.toInt() > 0 else true
                    ) {
                        Icon(Icons.Default.Block, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Banear")
                    }
                }
            }
        }
    }
}
