package com.example.saborforaneo.ui.screens.community

import android.app.Application
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.saborforaneo.util.Categorias
import com.example.saborforaneo.util.ValidacionConstantes
import com.example.saborforaneo.util.validarLongitudMax
import com.example.saborforaneo.util.esURLImagenValida
import com.example.saborforaneo.util.esNumeroEnRango
import com.example.saborforaneo.util.contarLineasNoVacias
import com.example.saborforaneo.util.porcentajeDeUso
import com.example.saborforaneo.viewmodel.ComunidadViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearReceta(
    navegarAtras: () -> Unit,
    viewModel: ComunidadViewModel = run {
        val context = androidx.compose.ui.platform.LocalContext.current
        viewModel {
            ComunidadViewModel(context.applicationContext as Application)
        }
    }
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
                            if (!nombre.validarLongitudMax(ValidacionConstantes.NOMBRE_RECETA_MAX)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("El nombre no puede exceder ${ValidacionConstantes.NOMBRE_RECETA_MAX} caracteres")
                                }
                                return@TextButton
                            }
                            if (descripcion.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Por favor ingresa una descripción")
                                }
                                return@TextButton
                            }
                            if (!descripcion.validarLongitudMax(ValidacionConstantes.DESCRIPCION_MAX)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("La descripción no puede exceder ${ValidacionConstantes.DESCRIPCION_MAX} caracteres")
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
                            if (!imagenUrl.validarLongitudMax(ValidacionConstantes.URL_MAX)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("La URL no puede exceder ${ValidacionConstantes.URL_MAX} caracteres")
                                }
                                return@TextButton
                            }
                            if (!imagenUrl.esURLImagenValida()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Por favor ingresa una URL de imagen válida (debe comenzar con http:// o https://)")
                                }
                                return@TextButton
                            }

                            val errorTiempo = tiempoPreparacion.esNumeroEnRango(ValidacionConstantes.TIEMPO_MIN, ValidacionConstantes.TIEMPO_MAX)
                            if (errorTiempo != null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Tiempo de preparación: $errorTiempo")
                                }
                                return@TextButton
                            }
                            val tiempo = tiempoPreparacion.toIntOrNull() ?: 0

                            val errorPorciones = porciones.esNumeroEnRango(ValidacionConstantes.PORCIONES_MIN, ValidacionConstantes.PORCIONES_MAX)
                            if (errorPorciones != null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Porciones: $errorPorciones")
                                }
                                return@TextButton
                            }
                            val porcionesNum = porciones.toIntOrNull() ?: 1

                            val ingredientesFiltrados = ingredientes.filter { it.isNotBlank() }
                            if (ingredientesFiltrados.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Agrega al menos un ingrediente")
                                }
                                return@TextButton
                            }
                            if (ingredientesFiltrados.size > ValidacionConstantes.INGREDIENTES_MAX) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Máximo ${ValidacionConstantes.INGREDIENTES_MAX} ingredientes permitidos")
                                }
                                return@TextButton
                            }
                            val ingredienteLargo = ingredientesFiltrados.find { !it.validarLongitudMax(ValidacionConstantes.INGREDIENTE_MAX_CHARS) }
                            if (ingredienteLargo != null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Cada ingrediente no puede exceder ${ValidacionConstantes.INGREDIENTE_MAX_CHARS} caracteres")
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
                            if (pasosFiltrados.size > ValidacionConstantes.PASOS_MAX) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Máximo ${ValidacionConstantes.PASOS_MAX} pasos permitidos")
                                }
                                return@TextButton
                            }
                            val pasoLargo = pasosFiltrados.find { !it.validarLongitudMax(ValidacionConstantes.PASO_MAX_CHARS) }
                            if (pasoLargo != null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Cada paso no puede exceder ${ValidacionConstantes.PASO_MAX_CHARS} caracteres")
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
                    onValueChange = { 
                        if (it.length <= ValidacionConstantes.URL_MAX) {
                            imagenUrl = it 
                        }
                    },
                    label = { Text("URL de la imagen *") },
                    placeholder = { Text("https://ejemplo.com/imagen.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.AddPhotoAlternate, null)
                    },
                    supportingText = {
                        if (imagenUrl.isNotEmpty() && !imagenUrl.esURLImagenValida()) {
                            Text(
                                "URL inválida. Debe comenzar con http:// o https://",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                "${imagenUrl.length}/${ValidacionConstantes.URL_MAX}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    isError = imagenUrl.isNotEmpty() && !imagenUrl.esURLImagenValida()
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
                    onValueChange = { 
                        if (it.length <= ValidacionConstantes.NOMBRE_RECETA_MAX) {
                            nombre = it 
                        }
                    },
                    label = { Text("Nombre de la receta *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text(
                            "${nombre.length}/${ValidacionConstantes.NOMBRE_RECETA_MAX}",
                            color = if (nombre.porcentajeDeUso(ValidacionConstantes.NOMBRE_RECETA_MAX) >= 80f) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = !nombre.validarLongitudMax(ValidacionConstantes.NOMBRE_RECETA_MAX)
                )
            }

            // Descripción
            item {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { 
                        if (it.length <= ValidacionConstantes.DESCRIPCION_MAX) {
                            descripcion = it 
                        }
                    },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    supportingText = {
                        Text(
                            "${descripcion.length}/${ValidacionConstantes.DESCRIPCION_MAX}",
                            color = if (descripcion.porcentajeDeUso(ValidacionConstantes.DESCRIPCION_MAX) >= 80f) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = !descripcion.validarLongitudMax(ValidacionConstantes.DESCRIPCION_MAX)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = tiempoPreparacion,
                            onValueChange = { 
                                if (it.all { char -> char.isDigit() } && it.length <= 4) {
                                    tiempoPreparacion = it
                                }
                            },
                            label = { Text("Tiempo (min) *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Timer, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = porciones,
                            onValueChange = { 
                                if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                    porciones = it
                                }
                            },
                            label = { Text("Porciones *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Restaurant, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
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
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = ingrediente,
                        onValueChange = { newValue ->
                            if (newValue.length <= ValidacionConstantes.INGREDIENTE_MAX_CHARS) {
                                ingredientes = ingredientes.toMutableList().apply {
                                    this[index] = newValue
                                }
                            }
                        },
                        label = { Text("Ingrediente ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        supportingText = {
                            Text(
                                "${ingrediente.length}/${ValidacionConstantes.INGREDIENTE_MAX_CHARS}",
                                color = if (ingrediente.porcentajeDeUso(ValidacionConstantes.INGREDIENTE_MAX_CHARS) >= 80f) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        isError = !ingrediente.validarLongitudMax(ValidacionConstantes.INGREDIENTE_MAX_CHARS)
                    )
                    if (ingredientes.size > 1) {
                        IconButton(
                            onClick = {
                                ingredientes = ingredientes.toMutableList().apply {
                                    removeAt(index)
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item {
                TextButton(
                    onClick = {
                        if (ingredientes.size < ValidacionConstantes.INGREDIENTES_MAX) {
                            ingredientes = ingredientes + ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = ingredientes.size < ValidacionConstantes.INGREDIENTES_MAX
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (ingredientes.size < ValidacionConstantes.INGREDIENTES_MAX)
                            "Agregar ingrediente (${ingredientes.size}/${ValidacionConstantes.INGREDIENTES_MAX})"
                        else
                            "Máximo de ingredientes alcanzado"
                    )
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
                        if (pasos.size < ValidacionConstantes.PASOS_MAX) {
                            pasos = pasos + ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pasos.size < ValidacionConstantes.PASOS_MAX
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (pasos.size < ValidacionConstantes.PASOS_MAX)
                            "Agregar paso (${pasos.size}/${ValidacionConstantes.PASOS_MAX})"
                        else
                            "Máximo de pasos alcanzado"
                    )
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

