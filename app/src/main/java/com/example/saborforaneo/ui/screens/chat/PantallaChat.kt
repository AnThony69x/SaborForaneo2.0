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
    
    // Sugerencias rápidas aleatorias
    val sugerenciasRapidas = remember {
        listOf(
            "🍽️ Receta del día" to "Dame una receta aleatoria del día",
            "🌮 Comida mexicana" to "Recomiéndame un platillo mexicano tradicional",
            "🥗 Opciones saludables" to "¿Qué puedo cocinar saludable hoy?",
            "⚡ Receta rápida" to "Dame una receta que se prepare en menos de 30 minutos",
            "🎂 Postres" to "Recomiéndame un postre delicioso",
            "🥘 Cocina regional" to "Cuéntame sobre platillos regionales de México",
            "👨‍🍳 Tips de cocina" to "Dame consejos útiles de cocina",
            "🌱 Vegetariano" to "Recomiéndame recetas vegetarianas"
        ).shuffled().take(4)
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
            // Header
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Chef AI Asistente",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isLoading) "Escribiendo..." else "En línea",
                                fontSize = 12.sp,
                                color = if (isLoading) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón limpiar chat
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Limpiar chat",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Botón cerrar
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.primary
                            )
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
                
                // Input de mensaje
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { 
                                if (it.length <= ValidacionConstantes.MENSAJE_CHAT_MAX) {
                                    messageText = it 
                                }
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Escribe tu mensaje...") },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            maxLines = 4,
                            supportingText = {
                                Text(
                                    "${messageText.length}/${ValidacionConstantes.MENSAJE_CHAT_MAX}",
                                    color = if (messageText.porcentajeDeUso(ValidacionConstantes.MENSAJE_CHAT_MAX) >= 80f) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            isError = !messageText.validarLongitudMax(ValidacionConstantes.MENSAJE_CHAT_MAX)
                        )

                        // Botón enviar
                        FloatingActionButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(messageText)
                                    messageText = ""
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp),
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 12.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Enviar mensaje",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
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
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Mensaje de bienvenida si no hay mensajes
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "¡Bienvenido al Chef AI!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Soy tu asistente culinario personal. Puedo ayudarte con recetas, consejos de cocina, sustitutos de ingredientes y mucho más.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "💡 Sugerencias rápidas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Chips de sugerencias rápidas
                    item {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sugerenciasRapidas) { (label, prompt) ->
                                SuggestionChip(
                                    onClick = {
                                        viewModel.sendMessage(prompt)
                                    },
                                    label = { Text(label) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    enabled = !isLoading,
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        labelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✨ Pregunta algo más",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(sugerenciasRapidas.take(3)) { (label, prompt) ->
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
                                        enabled = !isLoading
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
