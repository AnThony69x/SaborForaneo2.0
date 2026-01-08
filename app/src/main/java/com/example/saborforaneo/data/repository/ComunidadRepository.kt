package com.example.saborforaneo.data.repository

import android.net.Uri
import com.example.saborforaneo.data.model.ComentarioReceta
import com.example.saborforaneo.data.model.Dificultad
import com.example.saborforaneo.data.model.RecetaComunidad
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ComunidadRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageRepository = StorageRepository()

    companion object {
        const val COLLECTION_RECETAS_COMUNIDAD = "recetasComunidad"
        const val COLLECTION_COMENTARIOS = "comentarios"
    }

    /**
     * Obtener lista de IDs de recetas favoritas del usuario actual
     */
    private suspend fun obtenerFavoritosUsuario(): List<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return emptyList()
            val userDoc = db.collection("usuarios").document(uid).get().await()
            (userDoc.get("favoritosComunidad") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Observar recetas de la comunidad en tiempo real
     */
    fun observarRecetasComunidad(): Flow<List<RecetaComunidad>> = callbackFlow {
        val listener = db.collection(COLLECTION_RECETAS_COMUNIDAD)
            .whereEqualTo("activa", true)
            .whereEqualTo("moderada", true)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val recetas = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        RecetaComunidad(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            imagenUrl = doc.getString("imagenUrl") ?: "",
                            tiempoPreparacion = doc.getLong("tiempoPreparacion")?.toInt() ?: 0,
                            dificultad = try {
                                Dificultad.valueOf(doc.getString("dificultad") ?: "MEDIA")
                            } catch (e: Exception) {
                                Dificultad.MEDIA
                            },
                            porciones = doc.getLong("porciones")?.toInt() ?: 1,
                            categoria = doc.getString("categoria") ?: "",
                            ingredientes = (doc.get("ingredientes") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            pasos = (doc.get("pasos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            esVegetariana = doc.getBoolean("esVegetariana") ?: false,
                            esVegana = doc.getBoolean("esVegana") ?: false,
                            autorId = doc.getString("autorId") ?: doc.getString("autorUid") ?: "",
                            autorUid = doc.getString("autorUid") ?: doc.getString("autorId") ?: "",
                            nombreAutor = doc.getString("nombreAutor") ?: doc.getString("autorNombre") ?: "",
                            autorNombre = doc.getString("autorNombre") ?: doc.getString("nombreAutor") ?: "",
                            autorFoto = doc.getString("autorFoto") ?: "",
                            fechaCreacion = doc.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                            likes = doc.getLong("likes")?.toInt() ?: 0,
                            comentarios = doc.getLong("comentarios")?.toInt() ?: 0,
                            usuariosQueLikean = (doc.get("usuariosQueLikean") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            activa = doc.getBoolean("activa") ?: true,
                            moderada = doc.getBoolean("moderada") ?: true,
                            publicada = doc.getBoolean("publicada") ?: false,
                            rechazada = doc.getBoolean("rechazada") ?: false,
                            fechaPublicacion = doc.getLong("fechaPublicacion") ?: 0,
                            totalFavoritos = doc.getLong("totalFavoritos")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(recetas)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtener recetas del usuario actual
     */
    suspend fun obtenerMisRecetas(): Result<List<RecetaComunidad>> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            val snapshot = db.collection(COLLECTION_RECETAS_COMUNIDAD)
                .whereEqualTo("autorUid", uid)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val recetas = snapshot.documents.mapNotNull { doc ->
                try {
                    RecetaComunidad(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        imagenUrl = doc.getString("imagenUrl") ?: "",
                        tiempoPreparacion = doc.getLong("tiempoPreparacion")?.toInt() ?: 0,
                        dificultad = try {
                            Dificultad.valueOf(doc.getString("dificultad") ?: "MEDIA")
                        } catch (e: Exception) {
                            Dificultad.MEDIA
                        },
                        porciones = doc.getLong("porciones")?.toInt() ?: 1,
                        categoria = doc.getString("categoria") ?: "",
                        ingredientes = (doc.get("ingredientes") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        pasos = (doc.get("pasos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        esVegetariana = doc.getBoolean("esVegetariana") ?: false,
                        esVegana = doc.getBoolean("esVegana") ?: false,
                        autorUid = doc.getString("autorUid") ?: "",
                        autorNombre = doc.getString("autorNombre") ?: "",
                        autorFoto = doc.getString("autorFoto") ?: "",
                        fechaCreacion = doc.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                        likes = doc.getLong("likes")?.toInt() ?: 0,
                        comentarios = doc.getLong("comentarios")?.toInt() ?: 0,
                        usuariosQueLikean = (doc.get("usuariosQueLikean") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        activa = doc.getBoolean("activa") ?: true,
                        moderada = doc.getBoolean("moderada") ?: true
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(recetas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crear nueva receta en la comunidad
     * Usa imagenUrl directa (no sube a Storage)
     */
    suspend fun crearReceta(receta: RecetaComunidad, imageUri: Uri?): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            // Validar que la URL de imagen no esté vacía
            if (receta.imagenUrl.isEmpty()) {
                return Result.failure(Exception("Debes proporcionar una URL de imagen"))
            }

            // Crear documento de receta
            val docRef = db.collection(COLLECTION_RECETAS_COMUNIDAD).document()
            val recetaConDatos = receta.copy(
                id = docRef.id,
                autorUid = uid
            )

            docRef.set(recetaConDatos.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar receta propia
     * Usa imagenUrl directa (no sube a Storage)
     */
    suspend fun actualizarReceta(recetaId: String, receta: RecetaComunidad, imageUri: Uri?): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar que la receta pertenezca al usuario
            val doc = db.collection(COLLECTION_RECETAS_COMUNIDAD).document(recetaId).get().await()
            if (doc.getString("autorUid") != uid) {
                return Result.failure(Exception("No tienes permiso para editar esta receta"))
            }

            // Validar URL
            if (receta.imagenUrl.isEmpty()) {
                return Result.failure(Exception("Debes proporcionar una URL de imagen"))
            }

            // Preservar campos de moderación existentes
            val publicada = doc.getBoolean("publicada") ?: false
            val rechazada = doc.getBoolean("rechazada") ?: false
            val fechaPublicacion = doc.getLong("fechaPublicacion") ?: 0
            val totalFavoritos = doc.getLong("totalFavoritos")?.toInt() ?: 0

            // Crear mapa actualizado preservando campos de moderación
            val recetaActualizada = receta.copy(
                publicada = publicada,
                rechazada = rechazada,
                fechaPublicacion = fechaPublicacion,
                totalFavoritos = totalFavoritos
            )

            db.collection(COLLECTION_RECETAS_COMUNIDAD)
                .document(recetaId)
                .set(recetaActualizada.toMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Eliminar receta propia
     * No elimina imagen porque es URL externa
     */
    suspend fun eliminarReceta(recetaId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar que la receta pertenezca al usuario
            val doc = db.collection(COLLECTION_RECETAS_COMUNIDAD).document(recetaId).get().await()
            if (doc.getString("autorUid") != uid) {
                return Result.failure(Exception("No tienes permiso para eliminar esta receta"))
            }

            // Eliminar documento (la imagen es URL externa, no se elimina)
            db.collection(COLLECTION_RECETAS_COMUNIDAD).document(recetaId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle like en receta
     */
    suspend fun toggleLike(recetaId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            val docRef = db.collection(COLLECTION_RECETAS_COMUNIDAD).document(recetaId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val usuariosQueLikean = (snapshot.get("usuariosQueLikean") as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.toMutableList() ?: mutableListOf()

                if (usuariosQueLikean.contains(uid)) {
                    // Quitar like
                    transaction.update(docRef, "usuariosQueLikean", FieldValue.arrayRemove(uid))
                    transaction.update(docRef, "likes", FieldValue.increment(-1))
                } else {
                    // Agregar like
                    transaction.update(docRef, "usuariosQueLikean", FieldValue.arrayUnion(uid))
                    transaction.update(docRef, "likes", FieldValue.increment(1))
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Agregar comentario o respuesta
     */
    suspend fun agregarComentario(recetaId: String, comentario: String, parentId: String = ""): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            val usuario = FirestoreRepository().obtenerPerfilUsuario(uid).getOrNull()
                ?: return Result.failure(Exception("No se pudo obtener el perfil"))

            val comentarioData = ComentarioReceta(
                id = "",
                recetaId = recetaId,
                autorUid = uid,
                autorNombre = usuario.nombre,
                autorFoto = usuario.fotoPerfil,
                comentario = comentario,
                fechaCreacion = System.currentTimeMillis(),
                parentId = parentId
            )

            // Agregar comentario
            val docRef = db.collection(COLLECTION_COMENTARIOS).document()
            docRef.set(comentarioData.copy(id = docRef.id).toMap()).await()

            // Si es una respuesta, incrementar contador del comentario padre
            if (parentId.isNotEmpty()) {
                db.collection(COLLECTION_COMENTARIOS)
                    .document(parentId)
                    .update("respuestas", FieldValue.increment(1))
                    .await()
            } else {
                // Si es comentario principal, incrementar contador de la receta
                db.collection(COLLECTION_RECETAS_COMUNIDAD)
                    .document(recetaId)
                    .update("comentarios", FieldValue.increment(1))
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observar comentarios de una receta en tiempo real (solo comentarios principales)
     */
    fun observarComentarios(recetaId: String): Flow<List<ComentarioReceta>> = callbackFlow {
        val listener = db.collection(COLLECTION_COMENTARIOS)
            .whereEqualTo("recetaId", recetaId)
            .whereEqualTo("parentId", "")
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val comentarios = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        ComentarioReceta(
                            id = doc.id,
                            recetaId = doc.getString("recetaId") ?: "",
                            autorUid = doc.getString("autorUid") ?: "",
                            autorNombre = doc.getString("autorNombre") ?: "",
                            autorFoto = doc.getString("autorFoto") ?: "",
                            comentario = doc.getString("comentario") ?: "",
                            fechaCreacion = doc.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                            parentId = doc.getString("parentId") ?: "",
                            respuestas = doc.getLong("respuestas")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(comentarios)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtener comentarios de una receta (solo comentarios principales) - Versión síncrona
     */
    suspend fun obtenerComentarios(recetaId: String): Result<List<ComentarioReceta>> {
        return try {
            val snapshot = db.collection(COLLECTION_COMENTARIOS)
                .whereEqualTo("recetaId", recetaId)
                .whereEqualTo("parentId", "")
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val comentarios = snapshot.documents.mapNotNull { doc ->
                try {
                    ComentarioReceta(
                        id = doc.id,
                        recetaId = doc.getString("recetaId") ?: "",
                        autorUid = doc.getString("autorUid") ?: "",
                        autorNombre = doc.getString("autorNombre") ?: "",
                        autorFoto = doc.getString("autorFoto") ?: "",
                        comentario = doc.getString("comentario") ?: "",
                        fechaCreacion = doc.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                        parentId = doc.getString("parentId") ?: "",
                        respuestas = doc.getLong("respuestas")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(comentarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener respuestas de un comentario
     */
    suspend fun obtenerRespuestas(comentarioId: String): Result<List<ComentarioReceta>> {
        return try {
            val snapshot = db.collection(COLLECTION_COMENTARIOS)
                .whereEqualTo("parentId", comentarioId)
                .orderBy("fechaCreacion", Query.Direction.ASCENDING)
                .get()
                .await()

            val respuestas = snapshot.documents.mapNotNull { doc ->
                try {
                    ComentarioReceta(
                        id = doc.id,
                        recetaId = doc.getString("recetaId") ?: "",
                        autorUid = doc.getString("autorUid") ?: "",
                        autorNombre = doc.getString("autorNombre") ?: "",
                        autorFoto = doc.getString("autorFoto") ?: "",
                        comentario = doc.getString("comentario") ?: "",
                        fechaCreacion = doc.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                        parentId = doc.getString("parentId") ?: "",
                        respuestas = doc.getLong("respuestas")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(respuestas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Eliminar comentario
     */
    suspend fun eliminarComentario(comentarioId: String, recetaId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar que el comentario pertenezca al usuario
            val doc = db.collection(COLLECTION_COMENTARIOS).document(comentarioId).get().await()
            if (doc.getString("autorUid") != uid) {
                return Result.failure(Exception("No tienes permiso para eliminar este comentario"))
            }

            val parentId = doc.getString("parentId") ?: ""

            // Eliminar el comentario
            db.collection(COLLECTION_COMENTARIOS).document(comentarioId).delete().await()

            // Decrementar contador
            if (parentId.isNotEmpty()) {
                // Es una respuesta
                db.collection(COLLECTION_COMENTARIOS)
                    .document(parentId)
                    .update("respuestas", FieldValue.increment(-1))
                    .await()
            } else {
                // Es comentario principal
                db.collection(COLLECTION_RECETAS_COMUNIDAD)
                    .document(recetaId)
                    .update("comentarios", FieldValue.increment(-1))
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Editar comentario o respuesta
     */
    suspend fun editarComentario(comentarioId: String, nuevoTexto: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar que el comentario pertenezca al usuario
            val doc = db.collection(COLLECTION_COMENTARIOS).document(comentarioId).get().await()
            if (doc.getString("autorUid") != uid) {
                return Result.failure(Exception("No tienes permiso para editar este comentario"))
            }

            // Actualizar el texto del comentario
            db.collection(COLLECTION_COMENTARIOS)
                .document(comentarioId)
                .update("comentario", nuevoTexto)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener detalle de una receta de comunidad
     */
    suspend fun obtenerRecetaDetalle(recetaId: String): Result<RecetaComunidad?> {
        return try {
            val doc = db.collection(COLLECTION_RECETAS_COMUNIDAD).document(recetaId).get().await()
            
            // Obtener lista de favoritos del usuario
            val favoritosUsuario = obtenerFavoritosUsuario()

            val receta = if (doc.exists()) {
                RecetaComunidad(
                    id = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    descripcion = doc.getString("descripcion") ?: "",
                    imagenUrl = doc.getString("imagenUrl") ?: "",
                    tiempoPreparacion = doc.getLong("tiempoPreparacion")?.toInt() ?: 0,
                    dificultad = try {
                        Dificultad.valueOf(doc.getString("dificultad") ?: "MEDIA")
                    } catch (e: Exception) {
                        Dificultad.MEDIA
                    },
                    porciones = doc.getLong("porciones")?.toInt() ?: 1,
                    categoria = doc.getString("categoria") ?: "",
                    ingredientes = (doc.get("ingredientes") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    pasos = (doc.get("pasos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    esVegetariana = doc.getBoolean("esVegetariana") ?: false,
                    esVegana = doc.getBoolean("esVegana") ?: false,
                    autorUid = doc.getString("autorUid") ?: "",
                    autorNombre = doc.getString("autorNombre") ?: "",
                    autorFoto = doc.getString("autorFoto") ?: "",
                    fechaCreacion = doc.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                    likes = doc.getLong("likes")?.toInt() ?: 0,
                    comentarios = doc.getLong("comentarios")?.toInt() ?: 0,
                    usuariosQueLikean = (doc.get("usuariosQueLikean") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    activa = doc.getBoolean("activa") ?: true,
                    moderada = doc.getBoolean("moderada") ?: true,
                    esFavorito = favoritosUsuario.contains(recetaId)
                )
            } else {
                null
            }

            Result.success(receta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar estado de favorito de una receta de comunidad
     */
    suspend fun actualizarFavoritoRecetaComunidad(recetaId: String, esFavorito: Boolean): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            
            val userDoc = db.collection("usuarios").document(uid)
            
            if (esFavorito) {
                // Agregar a favoritos
                userDoc.update("favoritosComunidad", FieldValue.arrayUnion(recetaId)).await()
            } else {
                // Quitar de favoritos
                userDoc.update("favoritosComunidad", FieldValue.arrayRemove(recetaId)).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

