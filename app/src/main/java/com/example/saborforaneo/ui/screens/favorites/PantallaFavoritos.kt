package com.example.saborforaneo.ui.screens.favorites

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saborforaneo.data.mock.DatosMock
import com.example.saborforaneo.ui.components.BarraNavegacionInferior
import com.example.saborforaneo.ui.components.TarjetaReceta
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFavoritos(
    navegarADetalle: (String) -> Unit,
    controladorNav: NavController
) {
    val recetasFavoritas = remember { DatosMock.obtenerRecetasFavoritas() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mis Favoritos",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${recetasFavoritas.size} recetas guardadas",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(controladorNav = controladorNav)
        }
    ) { paddingValues ->
        if (recetasFavoritas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "❤️",
                        fontSize = 80.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aún no tienes favoritos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Guarda tus recetas favoritas para verlas aquí",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(recetasFavoritas) { index, receta ->
                    var visible by remember { mutableStateOf(false) }

                    LaunchedEffect(key1 = receta.id) {
                        delay(index * 50L)
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(
                                    initialOffsetX = { -it / 2 },
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