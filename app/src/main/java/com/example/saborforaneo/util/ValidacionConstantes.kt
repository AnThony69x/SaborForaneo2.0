package com.example.saborforaneo.util

import android.util.Patterns

/**
 * Constantes y funciones para validación de campos en toda la aplicación
 */
object ValidacionConstantes {
    // Límites de texto general
    const val NOMBRE_RECETA_MIN = 3
    const val NOMBRE_RECETA_MAX = 100
    const val DESCRIPCION_MAX = 500
    const val PAIS_MAX = 50
    const val NOMBRE_USUARIO_MIN = 3
    const val NOMBRE_USUARIO_MAX = 50
    const val EMAIL_MAX = 100
    const val PASSWORD_MIN = 6
    const val PASSWORD_MAX = 128
    
    // Límites de números
    const val TIEMPO_MIN = 1
    const val TIEMPO_MAX = 1440 // 24 horas en minutos
    const val PORCIONES_MIN = 1
    const val PORCIONES_MAX = 100
    
    // Límites de URL
    const val URL_MAX = 500
    
    // Límites de listas
    const val INGREDIENTES_MIN = 1
    const val INGREDIENTES_MAX = 50
    const val INGREDIENTE_MAX_CHARS = 200
    const val PASOS_MIN = 1
    const val PASOS_MAX = 30
    const val PASO_MAX_CHARS = 500
    const val INGREDIENTES_TEXTO_MAX = 10000
    const val PASOS_TEXTO_MAX = 15000
    
    // Límites de chat
    const val MENSAJE_CHAT_MAX = 1000
    const val BUSQUEDA_MAX = 100
    
    // Rate limiting
    const val MENSAJES_POR_MINUTO = 10
}

/**
 * Extensiones para validación de strings
 */

// Validar longitud máxima
fun String.validarLongitudMax(max: Int): Boolean = this.length <= max

// Validar longitud mínima
fun String.validarLongitudMin(min: Int): Boolean = this.length >= min

// Validar rango de longitud
fun String.validarLongitudRango(min: Int, max: Int): Boolean = 
    this.length in min..max

// Validar URL
fun String.esURLValida(): Boolean {
    if (this.isBlank()) return false
    if (!this.startsWith("http://") && !this.startsWith("https://")) return false
    return Patterns.WEB_URL.matcher(this).matches()
}

// Validar que sea una URL de imagen
fun String.esURLImagenValida(): Boolean {
    if (!this.esURLValida()) return false
    val extensionesValidas = listOf(".jpg", ".jpeg", ".png", ".webp", ".gif")
    val urlLower = this.lowercase()
    return extensionesValidas.any { urlLower.contains(it) } || 
           // Permitir URLs de servicios de imágenes conocidos
           urlLower.contains("unsplash.com") ||
           urlLower.contains("pexels.com") ||
           urlLower.contains("pixabay.com") ||
           urlLower.contains("imgur.com") ||
           urlLower.contains("cloudinary.com") ||
           urlLower.contains("googleusercontent.com")
}

// Solo letras, espacios y guiones
fun String.soloLetrasYEspacios(): Boolean = 
    this.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ \\-]+$"))

// Validar email
fun String.esEmailValido(): Boolean = 
    Patterns.EMAIL_ADDRESS.matcher(this).matches()

// Validar número en rango
fun String.esNumeroEnRango(min: Int, max: Int): String? {
    val numero = this.toIntOrNull()
    return when {
        numero == null -> "Debe ser un número válido"
        numero < min -> "El valor mínimo es $min"
        numero > max -> "El valor máximo es $max"
        else -> null // null = válido
    }
}

// Contar líneas no vacías
fun String.contarLineasNoVacias(): Int = 
    this.lines().count { it.isNotBlank() }

// Obtener porcentaje de uso de caracteres
fun String.porcentajeDeUso(max: Int): Float = 
    (this.length.toFloat() / max.toFloat()) * 100f

// Verificar si está cerca del límite (>80%)
fun String.cercaDelLimite(max: Int): Boolean = 
    porcentajeDeUso(max) >= 80f
