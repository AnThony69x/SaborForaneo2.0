package com.example.saborforaneo.data.model

data class Receta(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val imagenUrl: String,
    val tiempoPreparacion: Int,
    val dificultad: Dificultad,
    val porciones: Int,
    val categoria: String,
    val pais: String,
    val ingredientes: List<String>,
    val pasos: List<String>,
    val esFavorito: Boolean = false,
    val esVegetariana: Boolean = false,
    val esVegana: Boolean = false,
    val precio: Precio
)

enum class Dificultad {
    FACIL, MEDIA, DIFICIL
}

enum class Precio {
    ECONOMICO, MODERADO, COSTOSO
}