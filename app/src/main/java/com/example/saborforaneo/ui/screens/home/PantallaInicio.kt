package com.example.saborforaneo.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.saborforaneo.data.mock.DatosMock
import com.example.saborforaneo.ui.components.BarraNavegacionInferior
import com.example.saborforaneo.ui.components.ChipFiltro
import com.example.saborforaneo.ui.components.TarjetaReceta
import com.example.saborforaneo.ui.components.TarjetaRecetaSkeleton
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    navegarADetalle: (String) -> Unit,
    navegarABusqueda: () -> Unit,
    controladorNav: NavController
) {
    val contexto = LocalContext.current
    var recetasCargadas by remember { mutableStateOf(false) }
    var mostrarSkeletons by remember { mutableStateOf(true) }
    var ubicacionTexto by remember { mutableStateOf<String?>(null) }

    val tienePermisoUbicacion = remember {
        ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(key1 = tienePermisoUbicacion) {
        if (tienePermisoUbicacion) {
            try {
                val locationManager = contexto.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager

                @SuppressLint("MissingPermission")
                val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if (location != null) {
                    val geocoder = Geocoder(contexto, Locale.getDefault())

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0]
                                val ciudad = address.locality ?: address.subAdminArea
                                val provincia = address.adminArea
                                ubicacionTexto = if (ciudad != null && provincia != null) {
                                    "$ciudad, $provincia"
                                } else {
                                    provincia ?: "Ecuador"
                                }
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val ciudad = address.locality ?: address.subAdminArea
                            val provincia = address.adminArea
                            ubicacionTexto = if (ciudad != null && provincia != null) {
                                "$ciudad, $provincia"
                            } else {
                                provincia ?: "Ecuador"
                            }
                        }
                    }
                } else {
                    ubicacionTexto = "Ecuador"
                }
            } catch (e: Exception) {
                ubicacionTexto = "Ecuador"
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (!recetasCargadas) {
            DatosMock.cargarRecetas(contexto)
            delay(100)
            mostrarSkeletons = false
            recetasCargadas = true
        }
    }

    val recetas = remember(recetasCargadas) { DatosMock.recetasDestacadas }
    val categorias = remember { DatosMock.categorias }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }

    val recetasFiltradas = remember(categoriaSeleccionada, recetasCargadas) {
        if (categoriaSeleccionada == null) {
            recetas
        } else {
            when (categoriaSeleccionada) {
                "Rápidas" -> recetas.filter { it.tiempoPreparacion <= 30 }
                "Vegetariana" -> recetas.filter { it.esVegetariana }
                "Económica" -> recetas.filter { it.precio.name == "ECONOMICO" }
                else -> recetas.filter { receta ->
                    receta.categoria.contains(categoriaSeleccionada ?: "", ignoreCase = true)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SaborForáneo",
                            fontWeight = FontWeight.Bold
                        )
                        if (tienePermisoUbicacion && ubicacionTexto != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = ubicacionTexto ?: "Ecuador",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            Text(
                                text = "¿Qué cocinarás hoy?",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = navegarABusqueda) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Buscar recetas"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(controladorNav = controladorNav)
        }
    ) { paddingValues ->
        if (mostrarSkeletons) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(5) {
                    TarjetaRecetaSkeleton(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        } else {
            AnimatedVisibility(
                visible = recetasCargadas,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = "Categorías",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    ChipFiltro(
                                        texto = "Todas",
                                        seleccionado = categoriaSeleccionada == null,
                                        alSeleccionar = { categoriaSeleccionada = null }
                                    )
                                }

                                items(categorias) { categoria ->
                                    ChipFiltro(
                                        texto = "${categoria.icono} ${categoria.nombre}",
                                        seleccionado = categoriaSeleccionada == categoria.nombre,
                                        alSeleccionar = {
                                            categoriaSeleccionada = if (categoriaSeleccionada == categoria.nombre) {
                                                null
                                            } else {
                                                categoria.nombre
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = if (categoriaSeleccionada == null)
                                "Recetas Destacadas (${recetasFiltradas.size})"
                            else
                                "Recetas de $categoriaSeleccionada (${recetasFiltradas.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (recetasFiltradas.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🍽️",
                                        fontSize = 64.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No hay recetas disponibles",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Intenta con otra categoría",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(recetasFiltradas) { index, receta ->
                            var visible by remember { mutableStateOf(false) }

                            LaunchedEffect(key1 = receta.id) {
                                delay(index * 50L)
                                visible = true
                            }

                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(300)) +
                                        slideInVertically(
                                            initialOffsetY = { it / 2 },
                                            animationSpec = tween(300)
                                        )
                            ) {
                                TarjetaReceta(
                                    receta = receta,
                                    alHacerClic = { navegarADetalle(receta.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}