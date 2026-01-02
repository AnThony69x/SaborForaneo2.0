package com.example.saborforaneo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.saborforaneo.ui.navigation.Rutas

sealed class ItemNavegacion(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
) {
    object Inicio : ItemNavegacion(
        ruta = Rutas.Inicio.ruta,
        titulo = "Inicio",
        icono = Icons.Default.Home
    )

    object Busqueda : ItemNavegacion(
        ruta = Rutas.Busqueda.ruta,
        titulo = "Buscar",
        icono = Icons.Default.Search
    )

    object Favoritos : ItemNavegacion(
        ruta = Rutas.Favoritos.ruta,
        titulo = "Favoritos",
        icono = Icons.Default.Favorite
    )

    object Perfil : ItemNavegacion(
        ruta = Rutas.Perfil.ruta,
        titulo = "Perfil",
        icono = Icons.Default.Person
    )
}

@Composable
fun BarraNavegacionInferior(
    controladorNav: NavController
) {
    val items = listOf(
        ItemNavegacion.Inicio,
        ItemNavegacion.Busqueda,
        ItemNavegacion.Favoritos,
        ItemNavegacion.Perfil
    )

    val entradaActual = controladorNav.currentBackStackEntryAsState()
    val rutaActual = entradaActual.value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icono,
                        contentDescription = item.titulo
                    )
                },
                label = { Text(item.titulo) },
                selected = rutaActual == item.ruta,
                onClick = {
                    if (rutaActual != item.ruta) {
                        controladorNav.navigate(item.ruta) {
                            popUpTo(controladorNav.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}