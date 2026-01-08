package com.example.saborforaneo.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saborforaneo.ui.components.ChatMessageBubble
import com.example.saborforaneo.util.ValidacionConstantes
import com.example.saborforaneo.util.validarLongitudMax
import com.example.saborforaneo.util.porcentajeDeUso
import com.example.saborforaneo.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla del chat con Gemini AI mejorada con sugerencias rápidas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaChat(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Sugerencias rápidas aleatorias - más opciones
    val sugerenciasRapidas = remember {
        listOf(
            "🍽️ Receta del día" to "Dame una receta aleatoria del día",
            "🌮 Comida mexicana" to "Recomiéndame un platillo mexicano tradicional",
            "🥗 Opciones saludables" to "¿Qué puedo cocinar saludable hoy?",
            "⚡ Receta rápida" to "Dame una receta que se prepare en menos de 30 minutos",
            "🎂 Postres" to "Recomiéndame un postre delicioso",
            "🥘 Cocina regional" to "Cuéntame sobre platillos regionales de México",
            "👨‍🍳 Tips de cocina" to "Dame consejos útiles de cocina",
            "🌱 Vegetariano" to "Recomiéndame recetas vegetarianas",
            "🍕 Cena familiar" to "¿Qué puedo hacer para una cena familiar?",
            "🥙 Lunch para llevar" to "Dame ideas de comida para llevar al trabajo",
            "🍝 Pasta casera" to "Enséñame a hacer pasta desde cero",
            "🍲 Platillo con pollo" to "¿Qué puedo cocinar con pechuga de pollo?",
            "🧁 Repostería fácil" to "Dame una receta de repostería para principiantes",
            "🌶️ Comida picante" to "Recomiéndame platillos picantes mexicanos",
            "🥩 Carnes" to "¿Cómo preparar diferentes cortes de carne?",
            "🍜 Sopas y caldos" to "Dame recetas de sopas reconfortantes"
        ).shuffled().take(6)
    }

    // Auto-scroll cuando llega un nuevo mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(), // Importante: se eleva con el teclado
        topBar = {
            // Header mejorado con mejor espaciado y simetría
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column {
                    // Espaciador superior para bajar el contenido
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Icono del chef con fondo
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(28.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Chef AI Asistente",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isLoading) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else 
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Text(
                                    text = if (isLoading) "Escribiendo..." else "En línea",
                                    fontSize = 13.sp,
                                    color = if (isLoading) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Botón limpiar chat
                        IconButton(
                            onClick = { viewModel.clearChat() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Limpiar chat",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Botón cerrar
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                }
            }
        },
        bottomBar = {
            Column {
                // Mostrar error si existe
                AnimatedVisibility(
                    visible = error != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = error ?: "",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }

                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                
                // Input de mensaje mejorado
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = messageText,
                                    onValueChange = { 
                                        if (it.length <= ValidacionConstantes.MENSAJE_CHAT_MAX) {
                                            messageText = it 
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { 
                                        Text(
                                            "Pregúntame sobre recetas...",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        ) 
                                    },
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(28.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    maxLines = 4,
                                    isError = !messageText.validarLongitudMax(ValidacionConstantes.MENSAJE_CHAT_MAX)
                                )
                                
                                // Contador de caracteres
                                Text(
                                    "${messageText.length}/${ValidacionConstantes.MENSAJE_CHAT_MAX}",
                                    fontSize = 11.sp,
                                    color = if (messageText.porcentajeDeUso(ValidacionConstantes.MENSAJE_CHAT_MAX) >= 80f) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 4.dp)
                                )
                            }

                            // Botón enviar mejorado con validación
                            FloatingActionButton(
                                onClick = {
                                    val textoLimpio = messageText.trim()
                                    if (textoLimpio.isNotBlank() && textoLimpio.length >= 3) {
                                        viewModel.sendMessage(textoLimpio)
                                        messageText = ""
                                    }
                                },
                                containerColor = if (messageText.trim().isNotBlank() && messageText.trim().length >= 3 && !isLoading)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(56.dp)
                                    .padding(top = 8.dp),
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Enviar mensaje",
                                        tint = if (messageText.trim().isNotBlank() && messageText.trim().length >= 3)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Lista de mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mensaje de bienvenida si no hay mensajes
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Icono grande con fondo
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                modifier = Modifier.size(96.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .fillMaxSize(),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "¡Bienvenido al Chef AI!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Soy tu asistente culinario personal. Puedo ayudarte con recetas, consejos de cocina, sustitutos de ingredientes y mucho más.",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    lineHeight = 22.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Divider sutil
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth(0.3f),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "💡 Sugerencias para empezar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Chips de sugerencias rápidas
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            sugerenciasRapidas.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { (label, prompt) ->
                                        SuggestionChip(
                                            onClick = {
                                                viewModel.sendMessage(prompt)
                                            },
                                            label = { 
                                                Text(
                                                    label,
                                                    fontSize = 13.sp
                                                ) 
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            enabled = !isLoading,
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                iconContentColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Mensajes existentes
                    items(messages) { message ->
                        ChatMessageBubble(message = message)
                    }
                    
                    // Mostrar sugerencias también cuando hay mensajes
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth(0.25f),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            
                            Text(
                                text = "✨ Continúa la conversación",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // Sugerencias en cuadrícula
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                sugerenciasRapidas.take(4).chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { (label, prompt) ->
                                            AssistChip(
                                                onClick = {
                                                    viewModel.sendMessage(prompt)
                                                },
                                                label = { 
                                                    Text(
                                                        label,
                                                        fontSize = 12.sp
                                                    ) 
                                                },
                                                enabled = !isLoading,
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        // Si solo hay un item en la fila, agregar espacio
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
