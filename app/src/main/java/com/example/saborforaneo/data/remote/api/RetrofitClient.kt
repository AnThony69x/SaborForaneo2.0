package com.example.saborforaneo.data.remote.api

import com.example.saborforaneo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit singleton para consumir la API de Gemini
 */
object RetrofitClient {

    // La URL se lee desde BuildConfig (definida en app/build.gradle.kts a partir de secrets.properties)
    private val BASE_URL: String = BuildConfig.GEMINI_BASE_URL

    // Logging interceptor para ver las peticiones en Logcat (solo en desarrollo)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente HTTP con configuración de timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Instancia de Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API Service
    val geminiApi: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }
}
