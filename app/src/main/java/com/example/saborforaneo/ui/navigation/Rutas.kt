package com.example.saborforaneo.ui.navigation

sealed class Rutas(val ruta: String) {
    object Splash : Rutas("splash")
    object Onboarding : Rutas("onboarding")
    object Login : Rutas("login")
    object Registro : Rutas("registro")
    object RecuperarContrasena : Rutas("recuperar_contrasena")
    object TerminosCondiciones : Rutas("terminos_condiciones")
    object Inicio : Rutas("inicio")
    object Admin : Rutas("admin")
    object Busqueda : Rutas("busqueda")
    object Favoritos : Rutas("favoritos")
    object Perfil : Rutas("perfil")
    object DetalleReceta : Rutas("detalle/{recetaId}") {
        fun crearRuta(recetaId: String) = "detalle/$recetaId"
    }
}