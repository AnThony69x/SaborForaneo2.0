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
    
    // Rutas de navegación admin con barra inferior
    object InicioAdmin : Rutas("inicio_admin")
    object EstadisticasAdmin : Rutas("estadisticas_admin")
    object PerfilAdmin : Rutas("perfil_admin")
    
    // Rutas de gestión admin (sin barra inferior)
    object GestionRecetas : Rutas("gestion_recetas")
    object Dashboard : Rutas("dashboard")
    object GestionUsuarios : Rutas("gestion_usuarios")
    
    object Busqueda : Rutas("busqueda")
    object Favoritos : Rutas("favoritos")
    object Perfil : Rutas("perfil")
    object DetalleReceta : Rutas("detalle/{recetaId}") {
        fun crearRuta(recetaId: String) = "detalle/$recetaId"
    }
}