package com.example.saborforaneo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saborforaneo.data.model.Receta

@Composable
fun TarjetaReceta(
    receta: Receta,
    alHacerClic: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarDescripcion: Boolean = true
) {
    val contexto = LocalContext.current
    var esFavoritoLocal by remember { mutableStateOf(receta.esFavorito) }

    val escalaFavorito = remember { Animatable(1f) }
    val rotacionFavorito = remember { Animatable(0f) }

    LaunchedEffect(esFavoritoLocal) {
        if (esFavoritoLocal) {
            escalaFavorito.animateTo(
                targetValue = 1.4f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            rotacionFavorito.animateTo(
                targetValue = 360f,
                animationSpec = tween(400)
            )
            escalaFavorito.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
            rotacionFavorito.snapTo(0f)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { alHacerClic() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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
                            Text(
                                text = "🍽️",
                                fontSize = 64.sp
                            )
                        }
                    }
                }

                Surface(
                    onClick = { esFavoritoLocal = !esFavoritoLocal },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .graphicsLayer {
                            scaleX = escalaFavorito.value
                            scaleY = escalaFavorito.value
                            rotationZ = rotacionFavorito.value
                        },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Icon(
                        imageVector = if (esFavoritoLocal)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = if (esFavoritoLocal)
                            "Quitar de favoritos"
                        else
                            "Agregar a favoritos",
                        tint = if (esFavoritoLocal)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = receta.categoria,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = receta.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (mostrarDescripcion) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = receta.descripcion,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Tiempo",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${receta.tiempoPreparacion} min",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Porciones",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${receta.porciones} porciones",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (receta.dificultad.name) {
                            "FACIL" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            "MEDIA" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = when (receta.dificultad.name) {
                                "FACIL" -> "Fácil"
                                "MEDIA" -> "Media"
                                else -> "Difícil"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = when (receta.dificultad.name) {
                                "FACIL" -> MaterialTheme.colorScheme.tertiary
                                "MEDIA" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
    }
}