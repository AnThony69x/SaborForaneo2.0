package com.example.saborforaneo.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.saborforaneo.ui.navigation.Rutas

sealed class ItemNavegacionAdmin(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
) {
    object InicioAdmin : ItemNavegacionAdmin(
        ruta = Rutas.InicioAdmin.ruta,
        titulo = "Inicio",
        icono = Icons.Default.Dashboard
    )

    object EstadisticasAdmin : ItemNavegacionAdmin(
        ruta = Rutas.EstadisticasAdmin.ruta,
        titulo = "Stats",
        icono = Icons.Default.BarChart
    )

    object UsuariosAdmin : ItemNavegacionAdmin(
        ruta = Rutas.GestionComunidad.ruta,
        titulo = "Comunidad",
        icono = Icons.Default.Forum
    )

    object RecetasAdmin : ItemNavegacionAdmin(
        ruta = Rutas.GestionRecetas.ruta,
        titulo = "Recetas",
        icono = Icons.Default.Restaurant
    )

    object PerfilAdmin : ItemNavegacionAdmin(
        ruta = Rutas.PerfilAdmin.ruta,
        titulo = "Perfil",
        icono = Icons.Default.Person
    )
}

@Composable
fun BarraNavegacionInferiorAdmin(
    controladorNav: NavController
) {
    val items = listOf(
        ItemNavegacionAdmin.InicioAdmin,
        ItemNavegacionAdmin.EstadisticasAdmin,
        ItemNavegacionAdmin.UsuariosAdmin,
        ItemNavegacionAdmin.RecetasAdmin,
        ItemNavegacionAdmin.PerfilAdmin
    )

    val rutaActual = controladorNav.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = rutaActual == item.ruta
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icono,
                        contentDescription = item.titulo,
                        modifier = Modifier.size(if (isSelected) 28.dp else 24.dp)
                    )
                },
                label = null,
                selected = isSelected,
                onClick = {
                    if (rutaActual != item.ruta) {
                        controladorNav.navigate(item.ruta) {
                            // Limpiar el back stack hasta InicioAdmin para evitar acumulación
                            popUpTo(Rutas.InicioAdmin.ruta) {
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
