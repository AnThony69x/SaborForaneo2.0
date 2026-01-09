package com.example.saborforaneo.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.saborforaneo.data.model.Dificultad
import com.example.saborforaneo.data.model.Precio
import com.example.saborforaneo.data.model.Receta
import com.example.saborforaneo.util.Categorias
import com.example.saborforaneo.util.ValidacionConstantes
import com.example.saborforaneo.util.validarLongitudMax
import com.example.saborforaneo.util.esURLImagenValida
import com.example.saborforaneo.util.soloLetrasYEspacios
import com.example.saborforaneo.util.porcentajeDeUso
import com.example.saborforaneo.util.contarLineasNoVacias


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DialogoFormularioReceta(
    titulo: String,
    receta: Receta?,
    onDismiss: () -> Unit,
    onGuardar: (Receta) -> Unit
) {
    var nombre by remember { mutableStateOf(receta?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(receta?.descripcion ?: "") }
    var imagenUrl by remember { mutableStateOf(receta?.imagenUrl ?: "") }
    var tiempoPreparacion by remember { mutableStateOf(receta?.tiempoPreparacion?.toString() ?: "") }
    var dificultad by remember { mutableStateOf(receta?.dificultad ?: Dificultad.MEDIA) }
    var porciones by remember { mutableStateOf(receta?.porciones?.toString() ?: "1") }
    // Cambio: ahora es una lista de categorías seleccionadas
    var categoriasSeleccionadas by remember {
        mutableStateOf(
            if (receta?.categoria?.isNotBlank() == true)
                setOf(receta.categoria)
            else
                emptySet<String>()
        )
    }
    var pais by remember { mutableStateOf(receta?.pais ?: "") }
    var ingredientes by remember { mutableStateOf((receta?.ingredientes ?: emptyList()).joinToString("\n")) }
    var pasos by remember { mutableStateOf((receta?.pasos ?: emptyList()).joinToString("\n")) }
    var precio by remember { mutableStateOf(receta?.precio ?: Precio.MODERADO) }

    var mostrarMenuDificultad by remember { mutableStateOf(false) }
    var mostrarMenuPrecio by remember { mutableStateOf(false) }

    // Usar categorías centralizadas
    val categorias = Categorias.listaCompleta

    // Determinar automáticamente si es vegetariana/vegana según las categorías seleccionadas
    val esVegetariana = categoriasSeleccionadas.any { it.equals("Vegetariana", ignoreCase = true) } ||
                        categoriasSeleccionadas.any { it.equals("Vegana", ignoreCase = true) }
    val esVegana = categoriasSeleccionadas.any { it.equals("Vegana", ignoreCase = true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Encabezado
                TopAppBar(
                    title = { Text(titulo) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Cerrar")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                val nuevaReceta = Receta(
                                    id = receta?.id ?: "",
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    imagenUrl = imagenUrl,
                                    tiempoPreparacion = tiempoPreparacion.toIntOrNull() ?: 0,
                                    dificultad = dificultad,
                                    porciones = porciones.toIntOrNull() ?: 1,
                                    categoria = categoriasSeleccionadas.firstOrNull() ?: "", // Categoría principal
                                    pais = pais,
                                    ingredientes = ingredientes.split("\n").filter { it.isNotBlank() },
                                    pasos = pasos.split("\n").filter { it.isNotBlank() },
                                    esVegetariana = esVegetariana,
                                    esVegana = esVegana,
                                    precio = precio
                                )
                                onGuardar(nuevaReceta)
                            },
                            enabled = nombre.isNotBlank() && categoriasSeleccionadas.isNotEmpty()
                        ) {
                            Text("GUARDAR")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Formulario con secciones
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SECCIÓN: INFORMACIÓN BÁSICA
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "📝 Información Básica",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { 
                                    if (it.length <= ValidacionConstantes.NOMBRE_RECETA_MAX) {
                                        nombre = it 
                                    }
                                },
                                label = { Text("Nombre de la receta *") },
                                leadingIcon = { Icon(Icons.Default.Restaurant, null) },
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

                            OutlinedTextField(
                                value = descripcion,
                                onValueChange = { 
                                    if (it.length <= ValidacionConstantes.DESCRIPCION_MAX) {
                                        descripcion = it 
                                    }
                                },
                                label = { Text("Descripción") },
                                leadingIcon = { Icon(Icons.Default.Description, null) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 4,
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

                            OutlinedTextField(
                                value = imagenUrl,
                                onValueChange = { 
                                    if (it.length <= ValidacionConstantes.URL_MAX) {
                                        imagenUrl = it 
                                    }
                                },
                                label = { Text("URL de la imagen") },
                                leadingIcon = { Icon(Icons.Default.Image, null) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("https://images.unsplash.com/...") },
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

                            // Vista previa de la imagen
                            if (imagenUrl.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))

                                var imagenCargada by remember(imagenUrl) { mutableStateOf(false) }
                                var errorCarga by remember(imagenUrl) { mutableStateOf(false) }

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

                                            if (!imagenCargada && !errorCarga) {
                                                CircularProgressIndicator()
                                            }
                                        }
                                    }
                                }

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

                    // SECCIÓN: DETALLES
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "⚙️ Detalles",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = tiempoPreparacion,
                                    onValueChange = { tiempoPreparacion = it.filter { c -> c.isDigit() } },
                                    label = { Text("Tiempo (min)") },
                                    leadingIcon = { Icon(Icons.Default.Timer, null) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = porciones,
                                    onValueChange = { porciones = it.filter { c -> c.isDigit() } },
                                    label = { Text("Porciones") },
                                    leadingIcon = { Icon(Icons.Default.People, null) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }

                            // Selector de categorías múltiples
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Categorías *",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "(Selecciona una o más)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Chips de categorías en grid
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    categorias.forEach { cat ->
                                        val seleccionada = categoriasSeleccionadas.contains(cat)
                                        FilterChip(
                                            selected = seleccionada,
                                            onClick = {
                                                categoriasSeleccionadas = if (seleccionada) {
                                                    categoriasSeleccionadas - cat
                                                } else {
                                                    categoriasSeleccionadas + cat
                                                }
                                            },
                                            label = {
                                                Text("${Categorias.obtenerIcono(cat)} $cat")
                                            },
                                            leadingIcon = if (seleccionada) {
                                                {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                    }
                                }

                                // Mostrar categorías seleccionadas
                                if (categoriasSeleccionadas.isNotEmpty()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "Categorías seleccionadas:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                categoriasSeleccionadas.joinToString(", "),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = pais,
                                onValueChange = { 
                                    if (it.length <= ValidacionConstantes.PAIS_MAX) {
                                        pais = it 
                                    }
                                },
                                label = { Text("País de origen (opcional)") },
                                leadingIcon = { Icon(Icons.Default.Public, null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Ecuador, México, Italia...") },
                                supportingText = {
                                    if (pais.isNotEmpty() && !pais.soloLetrasYEspacios()) {
                                        Text(
                                            "Solo se permiten letras, espacios y guiones",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        Text(
                                            "${pais.length}/${ValidacionConstantes.PAIS_MAX}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                isError = pais.isNotEmpty() && !pais.soloLetrasYEspacios()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = mostrarMenuDificultad,
                                    onExpandedChange = { mostrarMenuDificultad = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = when(dificultad) {
                                            Dificultad.FACIL -> "Fácil"
                                            Dificultad.MEDIA -> "Media"
                                            Dificultad.DIFICIL -> "Difícil"
                                        },
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Dificultad") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mostrarMenuDificultad) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        singleLine = true
                                    )
                                    ExposedDropdownMenu(
                                        expanded = mostrarMenuDificultad,
                                        onDismissRequest = { mostrarMenuDificultad = false }
                                    ) {
                                        Dificultad.entries.forEach { dif ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(when(dif) {
                                                        Dificultad.FACIL -> "Fácil"
                                                        Dificultad.MEDIA -> "Media"
                                                        Dificultad.DIFICIL -> "Difícil"
                                                    })
                                                },
                                                onClick = {
                                                    dificultad = dif
                                                    mostrarMenuDificultad = false
                                                }
                                            )
                                        }
                                    }
                                }

                                ExposedDropdownMenuBox(
                                    expanded = mostrarMenuPrecio,
                                    onExpandedChange = { mostrarMenuPrecio = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = when(precio) {
                                            Precio.ECONOMICO -> "Bajo"
                                            Precio.MODERADO -> "Medio"
                                            Precio.COSTOSO -> "Alto"
                                        },
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Precio") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mostrarMenuPrecio) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        singleLine = true
                                    )
                                    ExposedDropdownMenu(
                                        expanded = mostrarMenuPrecio,
                                        onDismissRequest = { mostrarMenuPrecio = false }
                                    ) {
                                        Precio.entries.forEach { p ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(when(p) {
                                                        Precio.ECONOMICO -> "Bajo"
                                                        Precio.MODERADO -> "Medio"
                                                        Precio.COSTOSO -> "Alto"
                                                    })
                                                },
                                                onClick = {
                                                    precio = p
                                                    mostrarMenuPrecio = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Info sobre vegetariana/vegana automático
                            if (esVegetariana || esVegana) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("ℹ️", style = MaterialTheme.typography.titleMedium)
                                        Column {
                                            if (esVegana) {
                                                Text(
                                                    "Esta receta será marcada como Vegana 🌱",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            } else if (esVegetariana) {
                                                Text(
                                                    "Esta receta será marcada como Vegetariana 🥕",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECCIÓN: INGREDIENTES
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "🛒 Ingredientes",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = ingredientes,
                                onValueChange = { 
                                    if (it.length <= ValidacionConstantes.INGREDIENTES_TEXTO_MAX) {
                                        ingredientes = it 
                                    }
                                },
                                label = { Text("Escribe un ingrediente por línea") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 5,
                                maxLines = 8,
                                placeholder = {
                                    Text("Ejemplo:\n• 500g de pasta\n• 2 tomates grandes\n• 100ml aceite de oliva\n• Sal al gusto")
                                },
                                supportingText = {
                                    val lineas = ingredientes.contarLineasNoVacias()
                                    Text(
                                        "${ingredientes.length}/${ValidacionConstantes.INGREDIENTES_TEXTO_MAX} caracteres | $lineas líneas",
                                        color = if (lineas > ValidacionConstantes.INGREDIENTES_MAX || 
                                                   ingredientes.porcentajeDeUso(ValidacionConstantes.INGREDIENTES_TEXTO_MAX) >= 80f) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                isError = ingredientes.contarLineasNoVacias() > ValidacionConstantes.INGREDIENTES_MAX
                            )
                        }
                    }

                    // SECCIÓN: PREPARACIÓN
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "👨‍🍳 Pasos de Preparación",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = pasos,
                                onValueChange = { 
                                    if (it.length <= ValidacionConstantes.PASOS_TEXTO_MAX) {
                                        pasos = it 
                                    }
                                },
                                label = { Text("Escribe un paso por línea") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 6,
                                maxLines = 10,
                                placeholder = {
                                    Text("Ejemplo:\n1. Hervir agua con sal\n2. Cocinar la pasta 10 minutos\n3. Escurrir bien\n4. Servir caliente")
                                },
                                supportingText = {
                                    val lineas = pasos.contarLineasNoVacias()
                                    Text(
                                        "${pasos.length}/${ValidacionConstantes.PASOS_TEXTO_MAX} caracteres | $lineas líneas",
                                        color = if (lineas > ValidacionConstantes.PASOS_MAX || 
                                                   pasos.porcentajeDeUso(ValidacionConstantes.PASOS_TEXTO_MAX) >= 80f) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                isError = pasos.contarLineasNoVacias() > ValidacionConstantes.PASOS_MAX
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

