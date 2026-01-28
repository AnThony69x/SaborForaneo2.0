package com.example.saborforaneo.ui.screens.community

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.example.saborforaneo.data.model.ComentarioReceta
import com.example.saborforaneo.viewmodel.DetalleRecetaComunidadViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleRecetaComunidad(
    recetaId: String,
    navegarAtras: () -> Unit,
    scrollToComments: Boolean = false,
    viewModel: DetalleRecetaComunidadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val contexto = LocalContext.current

    LaunchedEffect(recetaId) {
        viewModel.cargarReceta(recetaId)
        viewModel.observarComentarios(recetaId)

        // Si debe hacer scroll a comentarios, activar el flag
        if (scrollToComments) {
            viewModel.activarScrollAComentarios()
        }
    }

    // Scroll automático a comentarios cuando se active el flag
    LaunchedEffect(uiState.scrollToComments) {
        if (uiState.scrollToComments) {
            // Esperar un poco para que el contenido se renderice
            kotlinx.coroutines.delay(300)
            // Hacer scroll al índice donde empiezan los comentarios
            // (aproximadamente después de imagen, info, ingredientes, pasos)
            listState.animateScrollToItem(8) // Ajustar según la estructura
            viewModel.desactivarScrollAComentarios()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Receta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = navegarAtras) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Botón de favoritos
                    uiState.receta?.let { receta ->
                        IconButton(onClick = {
                            viewModel.alternarFavorito(receta.id)
                        }) {
                            Icon(
                                imageVector = if (receta.esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (receta.esFavorito) "Quitar de favoritos" else "Agregar a favoritos",
                                tint = if (receta.esFavorito) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    // Botón de compartir
                    IconButton(onClick = {
                        uiState.receta?.let { receta ->
                            // Crear texto para compartir
                            val textoCompartir = buildString {
                                append("🍳 ${receta.nombre}\n\n")
                                append("📝 ${receta.descripcion}\n\n")
                                append("👤 Compartido por: ${receta.autorNombre}\n\n")
                                append("⏱️ Tiempo: ${receta.tiempoPreparacion} min\n")
                                append("🍽️ Porciones: ${receta.porciones}\n")
                                append("📊 Dificultad: ${receta.dificultad.name}\n")
                                append("🏷️ Categoría: ${receta.categoria}\n\n")

                                if (receta.ingredientes.isNotEmpty()) {
                                    append("🥗 Ingredientes:\n")
                                    receta.ingredientes.forEachIndexed { index, ingrediente ->
                                        append("${index + 1}. $ingrediente\n")
                                    }
                                    append("\n")
                                }

                                if (receta.pasos.isNotEmpty()) {
                                    append("👨‍🍳 Pasos:\n")
                                    receta.pasos.forEachIndexed { index, paso ->
                                        append("${index + 1}. $paso\n")
                                    }
                                }

                                append("\n💬 ${receta.comentarios} comentarios")
                                append("\n❤️ ${receta.likes} likes")
                                append("\n\n¡Descubre más recetas en SaborForáneo! 🎉")
                            }

                            // Crear Intent para compartir
                            val intentCompartir = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Receta: ${receta.nombre}")
                                putExtra(Intent.EXTRA_TEXT, textoCompartir)
                            }

                            // Mostrar selector de apps para compartir
                            val chooser = Intent.createChooser(intentCompartir, "Compartir receta vía...")
                            contexto.startActivity(chooser)
                        }
                    }) {
                        Icon(Icons.Default.Share, "Compartir receta")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.receta?.let { receta ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Imagen de la receta
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (receta.imagenUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(contexto)
                                        .data(receta.imagenUrl)
                                        .crossfade(true)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = receta.nombre,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // Información básica
                    item {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Autor
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                if (receta.autorFoto.isNotEmpty()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(contexto)
                                            .data(receta.autorFoto)
                                            .crossfade(true)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .build(),
                                        contentDescription = receta.autorNombre,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = receta.autorNombre.firstOrNull()?.toString()?.uppercase() ?: "?",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = receta.autorNombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = formatearFecha(receta.fechaCreacion),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Título
                            Text(
                                text = receta.nombre,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Descripción
                            Text(
                                text = receta.descripcion,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Info cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InfoCard(
                                    icon = "⏱️",
                                    label = "${receta.tiempoPreparacion} min",
                                    modifier = Modifier.weight(1f)
                                )
                                InfoCard(
                                    icon = "🍽️",
                                    label = "${receta.porciones} porciones",
                                    modifier = Modifier.weight(1f)
                                )
                                InfoCard(
                                    icon = "📊",
                                    label = receta.dificultad.name,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Likes y comentarios
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                                val leDioLike = receta.usuariosQueLikean.contains(currentUserId)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        viewModel.toggleLike(recetaId)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (leDioLike) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (leDioLike) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${receta.likes}",
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Comment,
                                        contentDescription = "Comentarios",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${receta.comentarios}",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Ingredientes
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "Ingredientes",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            receta.ingredientes.forEach { ingrediente ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text("• ", fontSize = 16.sp)
                                    Text(ingrediente, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Pasos
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "Preparación",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            receta.pasos.forEachIndexed { index, paso ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = paso,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                    item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Sección de comentarios
                    item {
                        Text(
                            text = "Comentarios (${uiState.comentarios.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Input de comentario
                    item {
                        var nuevoComentario by remember { mutableStateOf("") }
                        var enviando by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = nuevoComentario,
                                onValueChange = { nuevoComentario = it },
                                placeholder = { Text("Escribe un comentario...") },
                                modifier = Modifier.weight(1f),
                                maxLines = 3,
                                enabled = !enviando
                            )

                            if (enviando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        if (nuevoComentario.isNotBlank()) {
                                            enviando = true
                                            viewModel.agregarComentario(recetaId, nuevoComentario.trim())
                                            nuevoComentario = ""
                                            // Resetear estado después de delay
                                            scope.launch {
                                                kotlinx.coroutines.delay(1000)
                                                enviando = false
                                            }
                                        }
                                    },
                                    enabled = nuevoComentario.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Send, "Enviar")
                                }
                            }
                        }
                    }

                    // Lista de comentarios
                    items(
                        items = uiState.comentarios,
                        key = { it.id }
                    ) { comentario ->
                        ComentarioItem(
                            comentario = comentario,
                            onResponder = { comentarioId ->
                                viewModel.seleccionarComentarioParaResponder(comentarioId)
                            },
                            onEliminar = {
                                viewModel.eliminarComentario(it.id, recetaId)
                            },
                            onVerRespuestas = {
                                viewModel.cargarRespuestas(comentario.id)
                            },
                            respuestas = uiState.respuestasPorComentario[comentario.id] ?: emptyList(),
                            mostrarRespuestas = uiState.comentarioExpandido == comentario.id,
                            viewModel = viewModel,
                            recetaId = recetaId
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun InfoCard(icon: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ComentarioItem(
    comentario: ComentarioReceta,
    onResponder: (String) -> Unit,
    onEliminar: (ComentarioReceta) -> Unit,
    onVerRespuestas: () -> Unit,
    respuestas: List<ComentarioReceta>,
    mostrarRespuestas: Boolean,
    viewModel: DetalleRecetaComunidadViewModel,
    recetaId: String
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val esAutor = comentario.autorUid == currentUserId
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarInputRespuesta by remember { mutableStateOf(false) }
    var textoRespuesta by remember { mutableStateOf("") }
    var modoEdicion by remember { mutableStateOf(false) }
    var textoEditado by remember { mutableStateOf(comentario.comentario) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    // Foto del autor
                    if (comentario.autorFoto.isNotEmpty()) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(comentario.autorFoto)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = comentario.autorNombre,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = comentario.autorNombre.firstOrNull()?.toString()?.uppercase() ?: "?",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = comentario.autorNombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (esAutor) {
                                Row {
                                    // Botón editar
                                    IconButton(
                                        onClick = {
                                            modoEdicion = true
                                            textoEditado = comentario.comentario
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            "Editar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    // Botón eliminar
                                    IconButton(
                                        onClick = { mostrarDialogoEliminar = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            "Eliminar",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = formatearFecha(comentario.fechaCreacion),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Modo edición o mostrar texto
                        if (modoEdicion) {
                            OutlinedTextField(
                                value = textoEditado,
                                onValueChange = { textoEditado = it },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 5,
                                placeholder = { Text("Editar comentario...") }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Botón cancelar
                                TextButton(onClick = {
                                    modoEdicion = false
                                    textoEditado = comentario.comentario
                                }) {
                                    Text("Cancelar")
                                }

                                // Botón guardar
                                Button(
                                    onClick = {
                                        if (textoEditado.isNotBlank()) {
                                            viewModel.editarComentario(
                                                comentarioId = comentario.id,
                                                nuevoTexto = textoEditado.trim(),
                                                recetaId = recetaId,
                                                parentId = comentario.parentId
                                            )
                                            modoEdicion = false
                                        }
                                    },
                                    enabled = textoEditado.isNotBlank()
                                ) {
                                    Text("Guardar")
                                }
                            }
                        } else {
                            Text(
                                text = comentario.comentario,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botones de acción (solo si no está en modo edición)
                        if (!modoEdicion) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                TextButton(onClick = { mostrarInputRespuesta = !mostrarInputRespuesta }) {
                                    Icon(Icons.Default.Reply, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Responder", fontSize = 12.sp)
                                }

                                if (comentario.respuestas > 0) {
                                    TextButton(onClick = onVerRespuestas) {
                                        Icon(
                                            if (mostrarRespuestas) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${comentario.respuestas} respuestas", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Input para responder
                if (mostrarInputRespuesta) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = textoRespuesta,
                            onValueChange = { textoRespuesta = it },
                            placeholder = { Text("Escribe una respuesta...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        IconButton(
                            onClick = {
                                if (textoRespuesta.isNotBlank()) {
                                    viewModel.agregarRespuesta(recetaId, textoRespuesta, comentario.id)
                                    textoRespuesta = ""
                                    mostrarInputRespuesta = false
                                }
                            },
                            enabled = textoRespuesta.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, "Enviar", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Respuestas
        if (mostrarRespuestas && respuestas.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 48.dp, top = 8.dp)
            ) {
                respuestas.forEach { respuesta ->
                    RespuestaItem(
                        respuesta = respuesta,
                        onEliminar = {
                            viewModel.eliminarComentario(respuesta.id, recetaId)
                        },
                        viewModel = viewModel,
                        recetaId = recetaId
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar comentario") },
            text = { Text("¿Estás seguro de que quieres eliminar este comentario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEliminar = false
                        onEliminar(comentario)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun RespuestaItem(
    respuesta: ComentarioReceta,
    onEliminar: () -> Unit,
    viewModel: DetalleRecetaComunidadViewModel,
    recetaId: String
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val esAutor = respuesta.autorUid == currentUserId
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var modoEdicion by remember { mutableStateOf(false) }
    var textoEditado by remember { mutableStateOf(respuesta.comentario) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Foto del autor
            if (respuesta.autorFoto.isNotEmpty()) {
                val context = androidx.compose.ui.platform.LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(respuesta.autorFoto)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = respuesta.autorNombre,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = respuesta.autorNombre.firstOrNull()?.toString()?.uppercase() ?: "?",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = respuesta.autorNombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = formatearFecha(respuesta.fechaCreacion),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (esAutor) {
                        Row {
                            // Botón editar
                            IconButton(
                                onClick = {
                                    modoEdicion = true
                                    textoEditado = respuesta.comentario
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    "Editar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            // Botón eliminar
                            IconButton(
                                onClick = { mostrarDialogoEliminar = true },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    "Eliminar",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Modo edición o mostrar texto
                if (modoEdicion) {
                    OutlinedTextField(
                        value = textoEditado,
                        onValueChange = { textoEditado = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5,
                        placeholder = { Text("Editar respuesta...") },
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón cancelar
                        TextButton(onClick = {
                            modoEdicion = false
                            textoEditado = respuesta.comentario
                        }) {
                            Text("Cancelar", fontSize = 11.sp)
                        }

                        // Botón guardar
                        Button(
                            onClick = {
                                if (textoEditado.isNotBlank()) {
                                    viewModel.editarComentario(
                                        comentarioId = respuesta.id,
                                        nuevoTexto = textoEditado.trim(),
                                        recetaId = recetaId,
                                        parentId = respuesta.parentId
                                    )
                                    modoEdicion = false
                                }
                            },
                            enabled = textoEditado.isNotBlank()
                        ) {
                            Text("Guardar", fontSize = 11.sp)
                        }
                    }
                } else {
                    Text(
                        text = respuesta.comentario,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // Diálogo de confirmación
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar respuesta") },
            text = { Text("¿Estás seguro de que quieres eliminar esta respuesta?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEliminar = false
                        onEliminar()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

