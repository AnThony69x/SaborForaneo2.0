package com.example.saborforaneo.data.remote.firebase

/**
 * Constantes centralizadas para Firebase
 * Evita strings hardcodeados en toda la app
 */
object FirebaseConstants {
    // ==================== COLECCIONES ====================
    const val COLLECTION_USUARIOS = "usuarios"
    const val COLLECTION_RECETAS = "recetas"
    const val COLLECTION_FAVORITOS = "favoritos"
    const val COLLECTION_CATEGORIAS = "categorias"
    
    // ==================== STORAGE PATHS ====================
    const val STORAGE_USUARIOS = "usuarios"
    const val STORAGE_RECETAS = "recetas"
    const val STORAGE_TEMP = "temp"
    
    // ==================== CAMPOS DE USUARIO ====================
    const val FIELD_UID = "uid"
    const val FIELD_EMAIL = "email"
    const val FIELD_NOMBRE = "nombre"
    const val FIELD_FOTO_PERFIL = "fotoPerfil"
    const val FIELD_ROL = "rol"
    const val FIELD_FECHA_CREACION = "fechaCreacion"
    const val FIELD_RECETAS_FAVORITAS = "recetasFavoritas"
    const val FIELD_NOTIFICACIONES = "notificacionesActivas"
    const val FIELD_UBICACION = "ubicacionActiva"
    const val FIELD_TEMA_OSCURO = "temaOscuro"
    
    // ==================== ROLES ====================
    const val ROL_ADMIN = "admin"
    const val ROL_USUARIO = "usuario"
    
    // ==================== ADMIN ====================
    const val ADMIN_EMAIL = "saborforaneo@gmail.com"
    
    // ==================== LIMITES ====================
    const val LIMITE_RECETAS_QUERY = 20
    const val TIMEOUT_MILLIS = 30000L // 30 segundos
}
