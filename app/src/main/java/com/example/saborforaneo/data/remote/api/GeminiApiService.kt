package com.example.saborforaneo.data.remote.api

import com.example.saborforaneo.data.model.ChatRequest
import com.example.saborforaneo.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interface para definir los endpoints de la API de Gemini
 */
interface GeminiApiService {

    /**
     * Enviar un mensaje al chatbot de Gemini
     * @param request Objeto con el mensaje del usuario
     * @return Respuesta del chatbot
     */
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
}

