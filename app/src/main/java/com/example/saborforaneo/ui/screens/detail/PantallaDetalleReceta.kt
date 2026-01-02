package com.example.saborforaneo.ui.screens.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saborforaneo.data.mock.DatosMock
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleReceta(
    recetaId: String,
    navegarAtras: () -> Unit
) {
    val receta = remember { DatosMock.obtenerRecetaPorId(recetaId) }
    var esFavorito by remember { mutableStateOf(receta?.esFavorito ?: false) }
    val contexto = LocalContext.current

    val escalaImagen = remember { Animatable(0.8f) }
    val alphaImagen = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        alphaImagen.animateTo(
            targetValue = 1f,
            animationSpec = tween(400)
        )
        escalaImagen.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    if (receta == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Error") },
                    navigationIcon = {
                        IconButton(onClick = navegarAtras) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "❌",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Receta no encontrada",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = navegarAtras) {
                        Text("Volver al inicio")
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = navegarAtras) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        esFavorito = !esFavorito
                    }) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (esFavorito) "Quitar de favoritos" else "Agregar a favoritos",
                            tint = if (esFavorito) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .graphicsLayer {
                            scaleX = escalaImagen.value
                            scaleY = escalaImagen.value
                            alpha = alphaImagen.value
                        }
                ) {
                    if (receta.imagenUrl.startsWith("http")) {
                        AsyncImage(
                            model = receta.imagenUrl,
                            contentDescription = "Imagen de ${receta.nombre}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val drawableId = contexto.resources.getIdentifier(
                            receta.imagenUrl,
                            "drawable",
                            contexto.packageName
                        )

                        if (drawableId != 0) {
                            Image(
                                painter = painterResource(id = drawableId),
                                contentDescription = "Imagen de ${receta.nombre}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🍽️",
                                        fontSize = 80.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Imagen no disponible",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = receta.categoria,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = receta.pais,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = receta.nombre,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = receta.descripcion,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoChip(
                            icono = Icons.Default.AccessTime,
                            texto = "${receta.tiempoPreparacion} min",
                            descripcion = "Tiempo"
                        )

                        InfoChip(
                            icono = Icons.Default.Restaurant,
                            texto = "${receta.porciones}",
                            descripcion = "Porciones"
                        )

                        InfoChip(
                            icono = Icons.Default.TrendingUp,
                            texto = when (receta.dificultad.name) {
                                "FACIL" -> "Fácil"
                                "MEDIA" -> "Media"
                                else -> "Difícil"
                            },
                            descripcion = "Dificultad"
                        )

                        InfoChip(
                            icono = Icons.Default.AttachMoney,
                            texto = when (receta.precio.name) {
                                "ECONOMICO" -> "Bajo"
                                "MODERADO" -> "Medio"
                                else -> "Alto"
                            },
                            descripcion = "Costo"
                        )
                    }

                    if (receta.esVegetariana || receta.esVegana) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (receta.esVegetariana) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("🥗 Vegetariana") }
                                )
                            }
                            if (receta.esVegana) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("🌱 Vegana") }
                                )
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Ingredientes",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            itemsIndexed(receta.ingredientes) { index, ingrediente ->
                IngredienteItemAnimado(ingrediente = ingrediente, index = index)
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Preparación",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            itemsIndexed(receta.pasos) { index, paso ->
                PasoItemAnimado(numero = index + 1, paso = paso, index = index)
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun InfoChip(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    descripcion: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 2.dp
        ) {
            Icon(
                imageVector = icono,
                contentDescription = descripcion,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = descripcion,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}


@Composable
fun IngredienteItemAnimado(ingrediente: String, index: Int) {
    var marcado by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = ingrediente) {
        delay(index * 30L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 250,
                easing = FastOutSlowInEasing
            )
        ) + slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = tween(
                durationMillis = 250,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        Surface(
            onClick = { marcado = !marcado },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            color = if (marcado)
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (marcado) Icons.Default.CheckCircle else Icons.Default.Circle,
                    contentDescription = null,
                    tint = if (marcado)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = ingrediente,
                    fontSize = 15.sp,
                    color = if (marcado)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun PasoItemAnimado(numero: Int, paso: String, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = paso) {
        delay(index * 40L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 280,
                easing = FastOutSlowInEasing
            )
        ) + slideInHorizontally(
            initialOffsetX = { it / 3 },
            animationSpec = tween(
                durationMillis = 280,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
                tonalElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = numero.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = paso,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}