package com.example.saborforaneo.data.repository

import android.content.Context
import com.example.saborforaneo.data.model.Receta
import com.example.saborforaneo.data.model.Dificultad
import com.example.saborforaneo.data.model.Precio
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class RecetaRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val recetasCollection = db.collection("recetas")

    /**
     * Obtener todas las recetas activas desde Firestore
     */
    suspend fun obtenerTodasLasRecetas(): Result<List<Receta>> {
        return try {
            val recetas = recetasCollection
                .whereEqualTo("activa", true)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(Receta::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        // Si hay error al convertir, intentar manualmente
                        convertirDocumentoAReceta(doc.id, doc.data)
                    }
                }

            Result.success(recetas)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Obtener recetas por categoría
     */
    suspend fun obtenerRecetasPorCategoria(categoria: String): Result<List<Receta>> {
        return try {
            val todasLasRecetas = obtenerTodasLasRecetas().getOrNull() ?: emptyList()
            val recetasFiltradas = todasLasRecetas.filter { it.categoria == categoria }
            Result.success(recetasFiltradas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener receta por ID desde Firestore
     */
    suspend fun obtenerRecetaPorId(id: String): Result<Receta?> {
        return try {
            val documento = recetasCollection.document(id).get().await()
            if (documento.exists()) {
                val receta = try {
                    documento.toObject(Receta::class.java)?.copy(id = documento.id)
                } catch (e: Exception) {
                    convertirDocumentoAReceta(documento.id, documento.data)
                }
                Result.success(receta)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Buscar recetas por texto
     */
    suspend fun buscarRecetas(query: String): Result<List<Receta>> {
        return try {
            val todasLasRecetas = obtenerTodasLasRecetas().getOrNull() ?: emptyList()
            val recetasFiltradas = todasLasRecetas.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                it.descripcion.contains(query, ignoreCase = true) ||
                it.categoria.contains(query, ignoreCase = true) ||
                it.ingredientes.any { ing -> ing.contains(query, ignoreCase = true) }
            }
            Result.success(recetasFiltradas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== CRUD PARA ADMIN ====================

    /**
     * Agregar nueva receta (solo admin)
     */
    suspend fun agregarReceta(receta: Receta, userId: String): Result<String> {
        return try {
            val nuevaReceta = receta.copy(
                id = "", // Firestore asignará el ID
                creadoPor = userId,
                fechaCreacion = System.currentTimeMillis(),
                activa = true
            )

            val docRef = recetasCollection.add(nuevaReceta.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Actualizar receta existente (solo admin)
     */
    suspend fun actualizarReceta(id: String, receta: Receta): Result<Unit> {
        return try {

            recetasCollection.document(id)
                .set(receta.toMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Eliminar receta (lógicamente - marca como inactiva)
     * Solo admin
     */
    suspend fun eliminarReceta(id: String): Result<Unit> {
        return try {

            // Eliminación lógica (marcar como inactiva)
            recetasCollection.document(id)
                .update("activa", false)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Obtener todas las recetas (para panel de gestión del admin)
     */
    suspend fun obtenerRecetasAdmin(): Result<List<Receta>> {
        return try {
            val recetas = recetasCollection
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(Receta::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        convertirDocumentoAReceta(doc.id, doc.data)
                    }
                }

            Result.success(recetas)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ==================== UTILIDADES ====================

    /**
     * Convertir documento de Firestore a objeto Receta manualmente
     * (útil cuando hay problemas de deserialización automática)
     */
    private fun convertirDocumentoAReceta(id: String, data: Map<String, Any>?): Receta? {
        if (data == null) return null

        return try {
            Receta(
                id = id,
                nombre = data["nombre"] as? String ?: "",
                descripcion = data["descripcion"] as? String ?: "",
                imagenUrl = data["imagenUrl"] as? String ?: "",
                tiempoPreparacion = (data["tiempoPreparacion"] as? Long)?.toInt() ?: 0,
                dificultad = try {
                    Dificultad.valueOf(data["dificultad"] as? String ?: "MEDIA")
                } catch (e: Exception) {
                    Dificultad.MEDIA
                },
                porciones = (data["porciones"] as? Long)?.toInt() ?: 1,
                categoria = data["categoria"] as? String ?: "",
                pais = data["pais"] as? String ?: "",
                ingredientes = (data["ingredientes"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                pasos = (data["pasos"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                esFavorito = data["esFavorito"] as? Boolean ?: false,
                esVegetariana = data["esVegetariana"] as? Boolean ?: false,
                esVegana = data["esVegana"] as? Boolean ?: false,
                precio = try {
                    Precio.valueOf(data["precio"] as? String ?: "MODERADO")
                } catch (e: Exception) {
                    Precio.MODERADO
                },
                creadoPor = data["creadoPor"] as? String ?: "sistema",
                fechaCreacion = data["fechaCreacion"] as? Long ?: System.currentTimeMillis(),
                activa = data["activa"] as? Boolean ?: true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

