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
    object Comunidad : Rutas("comunidad")
    object CrearRecetaComunidad : Rutas("crear_receta_comunidad")
    object DetalleRecetaComunidad : Rutas("detalle_receta_comunidad/{recetaId}?scrollToComments={scrollToComments}") {
        fun crearRuta(recetaId: String, scrollToComments: Boolean = false) =
            "detalle_receta_comunidad/$recetaId?scrollToComments=$scrollToComments"
    }
    object EditarRecetaComunidad : Rutas("editar_receta_comunidad/{recetaId}") {
        fun crearRuta(recetaId: String) = "editar_receta_comunidad/$recetaId"
    }
    object DetalleReceta : Rutas("detalle/{recetaId}") {
        fun crearRuta(recetaId: String) = "detalle/$recetaId"
    }
}