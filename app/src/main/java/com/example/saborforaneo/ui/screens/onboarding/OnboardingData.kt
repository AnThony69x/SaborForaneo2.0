package com.example.saborforaneo.ui.screens.onboarding

data class PaginaOnboarding(
    val titulo: String,
    val descripcion: String,
    val emoji: String,
    val colorFondo: Long
)

object OnboardingData {
    val paginas = listOf(
        PaginaOnboarding(
            titulo = "Bienvenido a SaborForáneo",
            descripcion = "Descubre recetas deliciosas de Ecuador y el mundo entero. Cocina fácil, rico y barato.",
            emoji = "🍳",
            colorFondo = 0xFFFF7043
        ),
        PaginaOnboarding(
            titulo = "Explora Recetas del Mundo",
            descripcion = "Miles de recetas de diferentes países y culturas. Desde platos tradicionales hasta innovaciones culinarias.",
            emoji = "🌍",
            colorFondo = 0xFF66BB6A
        ),
        PaginaOnboarding(
            titulo = "Guarda tus Favoritas",
            descripcion = "Marca tus recetas preferidas y accede a ellas fácilmente cuando quieras cocinarlas de nuevo.",
            emoji = "❤️",
            colorFondo = 0xFF42A5F5
        ),
        PaginaOnboarding(
            titulo = "Cocina Fácil y Rápido",
            descripcion = "Instrucciones paso a paso, ingredientes claros y tiempos precisos. ¡Empieza a cocinar ahora!",
            emoji = "⚡",
            colorFondo = 0xFFFFCA28
        )
    )
}