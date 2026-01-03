package com.example.saborforaneo.data.model

data class Receta(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val imagenUrl: String = "",
    val tiempoPreparacion: Int = 0,
    val dificultad: Dificultad = Dificultad.MEDIA,
    val porciones: Int = 1,
    val categoria: String = "",
    val pais: String = "",
    val ingredientes: List<String> = emptyList(),
    val pasos: List<String> = emptyList(),
    val esFavorito: Boolean = false,
    val esVegetariana: Boolean = false,
    val esVegana: Boolean = false,
    val precio: Precio = Precio.MODERADO,
    // Campos para gestión en Firestore
    val creadoPor: String = "admin",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val activa: Boolean = true
) {
    // Conversión de enum a String para Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "descripcion" to descripcion,
            "imagenUrl" to imagenUrl,
            "tiempoPreparacion" to tiempoPreparacion,
            "dificultad" to dificultad.name,
            "porciones" to porciones,
            "categoria" to categoria,
            "pais" to pais,
            "ingredientes" to ingredientes,
            "pasos" to pasos,
            "esFavorito" to esFavorito,
            "esVegetariana" to esVegetariana,
            "esVegana" to esVegana,
            "precio" to precio.name,
            "creadoPor" to creadoPor,
            "fechaCreacion" to fechaCreacion,
            "activa" to activa
        )
    }
}

enum class Dificultad {
    FACIL, MEDIA, DIFICIL
}

enum class Precio {
    ECONOMICO, MODERADO, COSTOSO
}

