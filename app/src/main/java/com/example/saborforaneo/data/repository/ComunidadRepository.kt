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

