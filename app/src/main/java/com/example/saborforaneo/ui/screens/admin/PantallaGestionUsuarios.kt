package com.example.saborforaneo.ui.screens.admin

import androidx.compose.animation.*
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
import com.example.saborforaneo.ui.components.BarraNavegacionInferiorAdmin
import com.example.saborforaneo.viewmodel.AdminViewModel
import com.example.saborforaneo.viewmodel.UsuarioInfo
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionUsuarios(
    controladorNav: NavController
) {
    val context = LocalContext.current
    val viewModel = remember { AdminViewModel(context) }
    val usuarios by viewModel.usuarios.collectAsState()
    var busqueda by remember { mutableStateOf("") }

    // Cargar usuarios al entrar
    LaunchedEffect(Unit) {
        viewModel.cargarUsuarios()
    }

    val usuariosFiltrados = remember(usuarios.usuarios, busqueda) {
        if (busqueda.isEmpty()) {
            usuarios.usuarios
        } else {
            usuarios.usuarios.filter {
                it.nombre.contains(busqueda, ignoreCase = true) ||
                it.email.contains(busqueda, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Gestión de Usuarios",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${usuarios.usuarios.size} usuarios registrados",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarUsuarios() }) {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar por nombre o email...") },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            when {
                usuarios.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                usuarios.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error al cargar usuarios",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = usuarios.error ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.cargarUsuarios() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                usuariosFiltrados.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "👥",
                                fontSize = 64.sp
                            )
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
                                onCambiarRol = { nuevoRol ->
                                    viewModel.cambiarRolUsuario(usuario.uid, nuevoRol)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaUsuario(
    usuario: UsuarioInfo,
    onCambiarRol: (String) -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    val fechaFormato = remember(usuario.fechaCreacion) {
        if (usuario.fechaCreacion > 0) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(usuario.fechaCreacion))
        } else {
            "Fecha no disponible"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (usuario.rol == "admin")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (usuario.rol == "admin")
                                Icons.Default.AdminPanelSettings
                            else
                                Icons.Default.Person,
                            contentDescription = null,
                            tint = if (usuario.rol == "admin")
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = usuario.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (usuario.rol == "admin") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = usuario.email,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                IconButton(onClick = { mostrarDialogo = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones"
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    icono = Icons.Default.CalendarToday,
                    texto = fechaFormato
                )
                InfoChip(
                    icono = Icons.Default.Favorite,
                    texto = "${usuario.totalFavoritos} favoritos"
                )
                InfoChip(
                    icono = Icons.Default.Badge,
                    texto = usuario.rol.uppercase()
                )
            }
        }
    }

    // Diálogo de opciones
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Opciones de Usuario") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Usuario: ${usuario.nombre}")
                    Text("Email: ${usuario.email}")
                    Text("UID: ${usuario.uid.take(20)}...")
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "Cambiar rol:",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                if (usuario.rol == "usuario") {
                    TextButton(onClick = {
                        onCambiarRol("admin")
                        mostrarDialogo = false
                    }) {
                        Text("⬆️ Promover a Admin")
                    }
                } else {
                    TextButton(onClick = {
                        onCambiarRol("usuario")
                        mostrarDialogo = false
                    }) {
                        Text("⬇️ Degradar a Usuario")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun InfoChip(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
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
