package com.example.saborforaneo.ui.screens.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.saborforaneo.util.Categorias
import com.example.saborforaneo.viewmodel.ComunidadViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearReceta(
    navegarAtras: () -> Unit,
    viewModel: ComunidadViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var tiempoPreparacion by remember { mutableStateOf("") }
    var porciones by remember { mutableStateOf("") }
    var dificultad by remember { mutableStateOf("MEDIA") }
    var ingredientes by remember { mutableStateOf(listOf("")) }
    var pasos by remember { mutableStateOf(listOf("")) }
    var esVegetariana by remember { mutableStateOf(false) }
    var esVegana by remember { mutableStateOf(false) }
    var imagenUrl by remember { mutableStateOf("") }
    var mostrarDialogoCategoria by remember { mutableStateOf(false) }
    var mostrarDialogoDificultad by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Usar categorías centralizadas (mismas que el admin y que ve el usuario)
    val categorias = Categorias.lista
        .filter { it.nombre != "Todas" }  // Quitar "Todas"
        .map { "${it.icono} ${it.nombre}" }  // Formato: "🍝 Pastas"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Receta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = navegarAtras) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Validar campos
                            if (nombre.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Por favor ingresa el nombre de la receta")
                                }
                                return@TextButton
                            }
                            if (descripcion.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Por favor ingresa una descripción")
                                }
                                return@TextButton
                            }
                            if (categoria.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Por favor selecciona una categoría")
                                }
                                return@TextButton
                            }

                            if (imagenUrl.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Por favor ingresa la URL de la imagen")
                                }
                                return@TextButton
                            }

                            val tiempo = tiempoPreparacion.toIntOrNull()
                            if (tiempo == null || tiempo <= 0) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Por favor ingresa un tiempo válido")
                                }
                                return@TextButton
                            }

                            val porcionesNum = porciones.toIntOrNull()
                            if (porcionesNum == null || porcionesNum <= 0) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Por favor ingresa porciones válidas")
                                }
                                return@TextButton
                            }

                            val ingredientesFiltrados = ingredientes.filter { it.isNotBlank() }
                            if (ingredientesFiltrados.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Agrega al menos un ingrediente")
                                }
                                return@TextButton
                            }

                            val pasosFiltrados = pasos.filter { it.isNotBlank() }
                            if (pasosFiltrados.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Agrega al menos un paso")
                                }
                                return@TextButton
                            }

                            // Crear receta
                            viewModel.crearReceta(
                                nombre = nombre,
                                descripcion = descripcion,
                                categoria = categoria,
                                tiempoPreparacion = tiempo,
                                porciones = porcionesNum,
                                dificultad = dificultad,
                                ingredientes = ingredientesFiltrados,
                                pasos = pasosFiltrados,
                                esVegetariana = esVegetariana,
                                esVegana = esVegana,
                                imagenUrl = imagenUrl,
                                imageUri = null,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("¡Receta creada exitosamente!")
                                    }
                                    navegarAtras()
                                },
                                onError = { error ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Error: $error")
                                    }
                                }
                            )
                        }
                    ) {
                        Text("PUBLICAR", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // URL de Imagen
            item {
                OutlinedTextField(
                    value = imagenUrl,
                    onValueChange = { imagenUrl = it },
                    label = { Text("URL de la imagen *") },
                    placeholder = { Text("https://ejemplo.com/imagen.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.AddPhotoAlternate, null)
                    }
                )
            }

            // Vista previa de la imagen (item separado)
            if (imagenUrl.isNotEmpty()) {
                item {
                    var imagenCargada by remember(imagenUrl) { mutableStateOf(false) }
                    var errorCarga by remember(imagenUrl) { mutableStateOf(false) }

                    Column {
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (errorCarga) {
                                    // Mostrar mensaje de error
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BrokenImage,
                                            contentDescription = "Error",
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "No se pudo cargar la imagen",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            "Verifica que la URL sea correcta",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    // Mostrar imagen
                                    AsyncImage(
                                        model = imagenUrl,
                                        contentDescription = "Vista previa",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        onSuccess = {
                                            imagenCargada = true
                                            errorCarga = false
                                        },
                                        onError = {
                                            imagenCargada = false
                                            errorCarga = true
                                        }
                                    )

                                    // Indicador de carga
                                    if (!imagenCargada && !errorCarga) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }

                        // Mensaje informativo
                        if (imagenCargada) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "✓ Imagen cargada correctamente",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            // Nombre
            item {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la receta *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Descripción
            item {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }

            // Categoría
            item {
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { },
                    label = { Text("Categoría *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarDialogoCategoria = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                )
            }

            // Tiempo y porciones
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tiempoPreparacion,
                        onValueChange = { if (it.all { char -> char.isDigit() }) tiempoPreparacion = it },
                        label = { Text("Tiempo (min) *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Timer, null) }
                    )
                    OutlinedTextField(
                        value = porciones,
                        onValueChange = { if (it.all { char -> char.isDigit() }) porciones = it },
                        label = { Text("Porciones *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Restaurant, null) }
                    )
                }
            }

            // Dificultad
            item {
                OutlinedTextField(
                    value = when (dificultad) {
                        "FACIL" -> "Fácil"
                        "MEDIA" -> "Media"
                        "DIFICIL" -> "Difícil"
                        else -> "Media"
                    },
                    onValueChange = { },
                    label = { Text("Dificultad") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarDialogoDificultad = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                )
            }

            // Ingredientes
            item {
                Text(
                    "Ingredientes *",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(ingredientes) { index, ingrediente ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = ingrediente,
                        onValueChange = { newValue ->
                            ingredientes = ingredientes.toMutableList().apply {
                                this[index] = newValue
                            }
                        },
                        label = { Text("Ingrediente ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    if (ingredientes.size > 1) {
                        IconButton(
                            onClick = {
                                ingredientes = ingredientes.toMutableList().apply {
                                    removeAt(index)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item {
                TextButton(
                    onClick = {
                        ingredientes = ingredientes + ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar ingrediente")
                }
            }

            // Pasos
            item {
                Text(
                    "Pasos de preparación *",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(pasos) { index, paso ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = paso,
                        onValueChange = { newValue ->
                            pasos = pasos.toMutableList().apply {
                                this[index] = newValue
                            }
                        },
                        label = { Text("Paso ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        minLines = 2,
                        maxLines = 4
                    )
                    if (pasos.size > 1) {
                        IconButton(
                            onClick = {
                                pasos = pasos.toMutableList().apply {
                                    removeAt(index)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item {
                TextButton(
                    onClick = {
                        pasos = pasos + ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar paso")
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    // Diálogo de categoría
    if (mostrarDialogoCategoria) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCategoria = false },
            title = { Text("Selecciona una categoría") },
            text = {
                LazyColumn {
                    items(categorias.size) { index ->
                        TextButton(
                            onClick = {
                                categoria = categorias[index]
                                mostrarDialogoCategoria = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                categorias[index],
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoCategoria = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de dificultad
    if (mostrarDialogoDificultad) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoDificultad = false },
            title = { Text("Selecciona la dificultad") },
            text = {
                Column {
                    listOf("FACIL" to "Fácil", "MEDIA" to "Media", "DIFICIL" to "Difícil").forEach { (value, label) ->
                        TextButton(
                            onClick = {
                                dificultad = value
                                mostrarDialogoDificultad = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoDificultad = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

