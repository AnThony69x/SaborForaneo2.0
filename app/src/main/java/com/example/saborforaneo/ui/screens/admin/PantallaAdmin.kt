package com.example.saborforaneo.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saborforaneo.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdmin(
    navegarALogin: () -> Unit,
    navegarAGestionRecetas: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    perfilViewModel: com.example.saborforaneo.ui.screens.profile.PerfilViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val usuarioFirestore by authViewModel.usuarioFirestore.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Panel de Administrador")
                        Text(
                            text = "Bienvenido, ${usuarioFirestore?.nombre ?: "Admin"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = {
                            // Cerrar sesión en Firebase (el AuthStateListener limpiará el estado automáticamente)
                            authViewModel.cerrarSesion()
                            // Navegar al login
                            navegarALogin()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de bienvenida
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Panel de Control",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Acceso completo al sistema",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Información del usuario
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Información del Administrador",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ItemInfo(
                        icono = Icons.Default.Person,
                        titulo = "Nombre",
                        valor = usuarioFirestore?.nombre ?: "Cargando..."
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ItemInfo(
                        icono = Icons.Default.Email,
                        titulo = "Email",
                        valor = currentUser?.email ?: "No disponible"
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ItemInfo(
                        icono = Icons.Default.Badge,
                        titulo = "Rol",
                        valor = usuarioFirestore?.rol?.uppercase() ?: "ADMIN"
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ItemInfo(
                        icono = Icons.Default.Key,
                        titulo = "UID",
                        valor = currentUser?.uid?.take(20) ?: "No disponible"
                    )
                }
            }

            // Secciones del panel de administrador
            Text(
                text = "Funcionalidades",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Tarjeta: Gestión de Recetas
            TarjetaOpcionAdmin(
                icono = Icons.Default.Restaurant,
                titulo = "Gestión de Recetas",
                descripcion = "Agregar, editar y eliminar recetas",
                habilitado = true,
                onClick = navegarAGestionRecetas
            )

            // Tarjeta: Gestión de Usuarios (Próximamente)
            TarjetaOpcionAdmin(
                icono = Icons.Default.People,
                titulo = "Gestión de Usuarios",
                descripcion = "Ver y administrar usuarios registrados",
                habilitado = false,
                onClick = { /* TODO */ }
            )

            // Tarjeta: Estadísticas (Próximamente)
            TarjetaOpcionAdmin(
                icono = Icons.Default.Analytics,
                titulo = "Estadísticas",
                descripcion = "Ver métricas y análisis de la app",
                habilitado = false,
                onClick = { /* TODO */ }
            )

            // Tarjeta: Notificaciones (Próximamente)
            TarjetaOpcionAdmin(
                icono = Icons.Default.Notifications,
                titulo = "Enviar Notificaciones",
                descripcion = "Enviar notificaciones a usuarios",
                habilitado = false,
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ItemInfo(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = valor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TarjetaOpcionAdmin(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    descripcion: String,
    habilitado: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (habilitado) onClick else { {} },
        enabled = habilitado,
        colors = CardDefaults.cardColors(
            containerColor = if (habilitado) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (habilitado)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = if (habilitado)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (habilitado)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (!habilitado) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Próximamente",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = descripcion,
                    fontSize = 13.sp,
                    color = if (habilitado)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            if (habilitado) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
