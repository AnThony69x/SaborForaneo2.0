package com.example.saborforaneo.util

data class Categoria(
    val nombre: String,
    val icono: String = "🍽️"
)

object Categorias {
    // Categorías principales que se muestran al usuario
    val lista = listOf(
        Categoria("Todas", "🍽️"),
        Categoria("Desayuno", "🍳"),
        Categoria("Almuerzo", "🍲"),
        Categoria("Cena", "🌙"),
        Categoria("Postre", "🍰"),
        Categoria("Bebidas", "🥤"),
        Categoria("Snacks", "🍿"),
        Categoria("Ensaladas", "🥗"),
        Categoria("Sopas", "🍜"),
        Categoria("Carnes", "🥩"),
        Categoria("Pescados", "🐟"),
        Categoria("Pastas", "🍝"),
        Categoria("Pizzas", "🍕"),
        Categoria("Vegetariana", "🥕"),
        Categoria("Vegana", "🌱"),
        Categoria("Rápidas", "⚡"),
        Categoria("Económica", "💰")
    )

    // Categorías para el admin: LAS MISMAS que ve el usuario (sin "Todas")
    val listaCompleta = lista
        .filter { it.nombre != "Todas" }  // Quitar "Todas" (es solo para filtros)
        .map { it.nombre }                 // Extraer solo los nombres

    // Obtener nombres simples (sin iconos) de las categorías del usuario
    val nombresUsuario = lista.filter { it.nombre != "Todas" }.map { it.nombre }

    // Verificar si una categoría es válida
    fun esValida(categoria: String): Boolean {
        return listaCompleta.any { it.equals(categoria, ignoreCase = true) }
    }

    // Obtener el icono de una categoría por su nombre
    fun obtenerIcono(nombreCategoria: String): String {
        return lista.find { it.nombre.equals(nombreCategoria, ignoreCase = true) }?.icono ?: "🍽️"
    }
}

