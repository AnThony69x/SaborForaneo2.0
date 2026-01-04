package com.example.saborforaneo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.model.ChatMessage
import com.example.saborforaneo.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar el estado del chat con Gemini
 */
class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    // Lista de mensajes del chat
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Mensaje de bienvenida
        addWelcomeMessage()
    }

    /**
     * Agregar mensaje de bienvenida
     */
    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            text = "¡Hola! 👋 Soy tu asistente culinario de SaborForáneo. Puedo ayudarte con recetas, consejos de cocina y mucho más. ¿En qué puedo ayudarte hoy?",
            isUser = false
        )
        _messages.value = listOf(welcomeMessage)
    }

    /**
     * Enviar mensaje al backend
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Agregar mensaje del usuario
        val userMessage = ChatMessage(
            text = text,
            isUser = true
        )
        _messages.value = _messages.value + userMessage

        // Agregar mensaje de "escribiendo..."
        val loadingMessage = ChatMessage(
            id = "loading",
            text = "Escribiendo...",
            isUser = false,
            isLoading = true
        )
        _messages.value = _messages.value + loadingMessage

        // Limpiar error previo
        _error.value = null
        _isLoading.value = true

        // Enviar al backend
        viewModelScope.launch {
            val result = repository.sendMessage(text)

            // Remover mensaje de carga
            _messages.value = _messages.value.filter { it.id != "loading" }

            result.fold(
                onSuccess = { response ->
                    // Agregar respuesta del bot
                    val botMessage = ChatMessage(
                        text = response.reply,
                        isUser = false
                    )
                    _messages.value = _messages.value + botMessage
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Error al enviar el mensaje"
                    _isLoading.value = false

                    // Agregar mensaje de error
                    val errorMessage = ChatMessage(
                        text = "Lo siento, hubo un error al procesar tu mensaje. Por favor, intenta nuevamente.",
                        isUser = false
                    )
                    _messages.value = _messages.value + errorMessage
                }
            )
        }
    }

    /**
     * Limpiar el chat
     */
    fun clearChat() {
        addWelcomeMessage()
        _error.value = null
    }

    /**
     * Limpiar error
     */
    fun clearError() {
        _error.value = null
    }
}

