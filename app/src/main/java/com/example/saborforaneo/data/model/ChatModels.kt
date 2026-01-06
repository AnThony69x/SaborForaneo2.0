package com.example.saborforaneo.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para enviar mensajes al backend
 */
data class ChatRequest(
    @SerializedName("message")
    val message: String
)

/**
 * Modelo para recibir respuestas del backend
 */
data class ChatResponse(
    @SerializedName("reply")
    val reply: String
)

/**
 * Modelo para representar un mensaje en la conversación
 */
data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)

