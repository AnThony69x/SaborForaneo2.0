package com.example.saborforaneo.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saborforaneo.ui.components.BarraBusqueda
import com.example.saborforaneo.ui.components.BarraNavegacionInferior
import com.example.saborforaneo.ui.components.ChipFiltro
import com.example.saborforaneo.ui.components.TarjetaReceta
import com.example.saborforaneo.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBusqueda(
    navegarADetalle: (String) -> Unit,
    navegarAComunidad: () -> Unit,
    navegarAtras: () -> Unit,
    controladorNav: NavController,
    homeViewModel: HomeViewModel
) {
    val uiState by homeViewModel.uiState.collectAsState()

    // Recargar favoritos cada vez que navegas de regreso a esta pantalla
    LaunchedEffect(Unit) {
        homeViewModel.recargarFavoritos()
    }

    var consultaBusqueda by remember { mutableStateOf("") }
    var filtroVegetariana by remember { mutableStateOf(false) }
    var filtroRapida by remember { mutableStateOf(false) }
    var filtroEconomica by remember { mutableStateOf(false) }

    val recetasFiltradas = remember(
        consultaBusqueda,
        filtroVegetariana,
        filtroRapida,
        filtroEconomica,
        uiState.recetas
    ) {
        uiState.recetas.filter { receta ->
            val coincideConsulta = if (consultaBusqueda.isEmpty()) {
                true
            } else {
                receta.nombre.contains(consultaBusqueda, ignoreCase = true) ||
                        receta.descripcion.contains(consultaBusqueda, ignoreCase = true) ||
                        receta.categoria.contains(consultaBusqueda, ignoreCase = true) ||
                        receta.ingredientes.any { it.contains(consultaBusqueda, ignoreCase = true) }
            }

            val cumpleFiltros = (!filtroVegetariana || receta.esVegetariana) &&
                    (!filtroRapida || receta.tiempoPreparacion <= 30) &&
                    (!filtroEconomica || receta.precio.name == "ECONOMICO")

            coincideConsulta && cumpleFiltros
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Recetas") },
                navigationIcon = {
                    IconButton(onClick = navegarAtras) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = navegarAComunidad,
                icon = {
                    Icon(Icons.Default.People, "Comunidad")
                },
                text = { Text("Comunidad") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        bottomBar = {
            BarraNavegacionInferior(controladorNav = controladorNav)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            BarraBusqueda(
                consulta = consultaBusqueda,
                alCambiarConsulta = { consultaBusqueda = it },
                placeholder = "Buscar por nombre, categoría, ingrediente...",
                modifier = Modifier.padding(16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    ChipFiltro(
                        texto = "🥕 Vegetariana",
                        seleccionado = filtroVegetariana,
                        alSeleccionar = { filtroVegetariana = !filtroVegetariana }
                    )
                }
                item {
                    ChipFiltro(
                        texto = "⚡ Rápida",
                        seleccionado = filtroRapida,
                        alSeleccionar = { filtroRapida = !filtroRapida }
                    )
                }
                item {
                    ChipFiltro(
                        texto = "💰 Económica",
                        seleccionado = filtroEconomica,
                        alSeleccionar = { filtroEconomica = !filtroEconomica }
                    )
                }
            }

            if (uiState.cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (recetasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🔍",
                            fontSize = 64.sp
                        )
                        Text(
                            text = "No se encontraron recetas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Intenta con otros términos de búsqueda",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Resultados (${recetasFiltradas.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    itemsIndexed(
                        items = recetasFiltradas,
                        key = { _, receta -> receta.id }
                    ) { index, receta ->
                        var visible by remember { mutableStateOf(false) }

                        LaunchedEffect(key1 = receta.id) {
                            delay(index * 50L)
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        ) {
                            TarjetaReceta(
                                receta = receta,
                                alHacerClick = { navegarADetalle(receta.id) },
                                esFavorito = receta.esFavorito,
                                onToggleFavorito = { homeViewModel.toggleFavorito(it) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

