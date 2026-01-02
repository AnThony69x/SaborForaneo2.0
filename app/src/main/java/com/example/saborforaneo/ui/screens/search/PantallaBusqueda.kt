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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saborforaneo.data.mock.DatosMock
import com.example.saborforaneo.ui.components.BarraBusqueda
import com.example.saborforaneo.ui.components.BarraNavegacionInferior
import com.example.saborforaneo.ui.components.ChipFiltro
import com.example.saborforaneo.ui.components.TarjetaReceta
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBusqueda(
    navegarADetalle: (String) -> Unit,
    navegarAtras: () -> Unit,
    controladorNav: NavController
) {
    val contexto = LocalContext.current
    var recetasCargadas by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        if (!recetasCargadas) {
            DatosMock.cargarRecetas(contexto)
            recetasCargadas = true
        }
    }

    var consultaBusqueda by remember { mutableStateOf("") }
    var filtroVegetariana by remember { mutableStateOf(false) }
    var filtroRapida by remember { mutableStateOf(false) }
    var filtroEconomica by remember { mutableStateOf(false) }

    val todasLasRecetas = remember(recetasCargadas) { DatosMock.recetasDestacadas }

    val recetasFiltradas = remember(
        consultaBusqueda,
        filtroVegetariana,
        filtroRapida,
        filtroEconomica,
        recetasCargadas
    ) {
        todasLasRecetas.filter { receta ->
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
                placeholder = "Buscar por nombre, ingrediente...",
                modifier = Modifier.padding(16.dp)
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Filtros",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        ChipFiltro(
                            texto = "🥗 Vegetariana",
                            seleccionado = filtroVegetariana,
                            alSeleccionar = { filtroVegetariana = !filtroVegetariana }
                        )
                    }
                    item {
                        ChipFiltro(
                            texto = "⚡ Rápidas (<30 min)",
                            seleccionado = filtroRapida,
                            alSeleccionar = { filtroRapida = !filtroRapida }
                        )
                    }
                    item {
                        ChipFiltro(
                            texto = "💰 Económicas",
                            seleccionado = filtroEconomica,
                            alSeleccionar = { filtroEconomica = !filtroEconomica }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            if (!recetasCargadas) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Text(
                            text = if (consultaBusqueda.isEmpty() && !filtroVegetariana && !filtroRapida && !filtroEconomica) {
                                "Todas las recetas (${recetasFiltradas.size})"
                            } else {
                                "Resultados encontrados: ${recetasFiltradas.size}"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
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
                                        text = "🔍",
                                        fontSize = 64.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No se encontraron recetas",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Intenta con otros términos o filtros",
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