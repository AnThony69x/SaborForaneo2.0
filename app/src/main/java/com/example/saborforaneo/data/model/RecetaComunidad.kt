package com.example.saborforaneo.data.model

data class RecetaComunidad(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val imagenUrl: String = "",
    val tiempoPreparacion: Int = 0,
    val dificultad: Dificultad = Dificultad.MEDIA,
    val porciones: Int = 1,
    val categoria: String = "",
    val ingredientes: List<String> = emptyList(),
    val pasos: List<String> = emptyList(),
    val esVegetariana: Boolean = false,
    val esVegana: Boolean = false,

    // Campos específicos de comunidad
    val autorId: String = "", // Alias para autorUid
    val autorUid: String = "",
    val nombreAutor: String = "", // Alias para autorNombre
    val autorNombre: String = "",
    val autorFoto: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val comentarios: Int = 0,
    val usuariosQueLikean: List<String> = emptyList(),

    // Estado de moderación
    val activa: Boolean = true,
    val moderada: Boolean = true,
    val publicada: Boolean = false,
    val rechazada: Boolean = false,
    val fechaPublicacion: Long = 0,
    
    // Estadísticas
    val totalFavoritos: Int = 0,
    
    // Favorito del usuario actual
    val esFavorito: Boolean = false
) {
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
            "ingredientes" to ingredientes,
            "pasos" to pasos,
            "esVegetariana" to esVegetariana,
            "esVegana" to esVegana,
            "autorId" to (autorId.ifEmpty { autorUid }),
            "autorUid" to autorUid,
            "nombreAutor" to (nombreAutor.ifEmpty { autorNombre }),
            "autorNombre" to autorNombre,
            "autorFoto" to autorFoto,
            "fechaCreacion" to fechaCreacion,
            "likes" to likes,
            "comentarios" to comentarios,
            "usuariosQueLikean" to usuariosQueLikean,
            "activa" to activa,
            "moderada" to moderada,
            "publicada" to publicada,
            "rechazada" to rechazada,
            "fechaPublicacion" to fechaPublicacion,
            "totalFavoritos" to totalFavoritos
        )
    }
}

data class ComentarioReceta(
    val id: String = "",
    val recetaId: String = "",
    val autorUid: String = "",
    val autorNombre: String = "",
    val autorFoto: String = "",
    val comentario: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val parentId: String = "", // ID del comentario padre (si es respuesta)
    val respuestas: Int = 0 // Contador de respuestas
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "recetaId" to recetaId,
            "autorUid" to autorUid,
            "autorNombre" to autorNombre,
            "autorFoto" to autorFoto,
            "comentario" to comentario,
            "fechaCreacion" to fechaCreacion,
            "parentId" to parentId,
            "respuestas" to respuestas
        )
    }
}

