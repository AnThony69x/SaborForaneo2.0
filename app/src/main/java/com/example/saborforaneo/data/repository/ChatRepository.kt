package com.example.saborforaneo.data.repository

import com.example.saborforaneo.data.model.ChatRequest
import com.example.saborforaneo.data.model.ChatResponse
import com.example.saborforaneo.data.remote.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para manejar las operaciones del chat con Gemini
 */
class ChatRepository {

    private val api = RetrofitClient.geminiApi

    /**
     * Enviar mensaje al backend de Gemini
     */
    suspend fun sendMessage(message: String): Result<ChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ChatRequest(message = message)
                val response = api.sendMessage(request)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

