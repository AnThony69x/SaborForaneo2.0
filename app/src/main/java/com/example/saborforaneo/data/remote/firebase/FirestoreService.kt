package com.example.saborforaneo.data.remote.firebase

import com.example.saborforaneo.data.repository.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Servicio para operaciones con Firestore
 * Maneja todas las consultas y escrituras a la base de datos
 */
class FirestoreService {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ==================== USUARIOS ====================
    
    /**
     * Crear o actualizar usuario en Firestore
     */
    suspend fun crearUsuario(usuario: Usuario): Result<Unit> {
        return try {
            db.collection(FirebaseConstants.COLLECTION_USUARIOS)
                .document(usuario.uid)
                .set(usuario, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener usuario por UID
     */
    suspend fun obtenerUsuario(uid: String): Result<Usuario?> {
        return try {
            val document = db.collection(FirebaseConstants.COLLECTION_USUARIOS)
                .document(uid)
                .get()
                .await()
            
            val usuario = document.toObject(Usuario::class.java)
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar un campo específico del usuario
     */
    suspend fun actualizarCampoUsuario(uid: String, campo: String, valor: Any): Result<Unit> {
        return try {
            db.collection(FirebaseConstants.COLLECTION_USUARIOS)
                .document(uid)
                .update(campo, valor)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar múltiples campos del usuario
     */
    suspend fun actualizarUsuario(uid: String, campos: Map<String, Any>): Result<Unit> {
        return try {
            db.collection(FirebaseConstants.COLLECTION_USUARIOS)
                .document(uid)
                .update(campos)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== FAVORITOS ====================
    
    /**
     * Agregar receta a favoritos
     */
    suspend fun agregarFavorito(uid: String, recetaId: String): Result<Unit> {
        return try {
            val userRef = db.collection(FirebaseConstants.COLLECTION_USUARIOS).document(uid)
            val documento = userRef.get().await()
            val favoritosActuales = documento.toObject(Usuario::class.java)?.recetasFavoritas ?: emptyList()
            
            if (!favoritosActuales.contains(recetaId)) {
                val nuevosFavoritos = favoritosActuales + recetaId
                userRef.update(FirebaseConstants.FIELD_RECETAS_FAVORITAS, nuevosFavoritos).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Quitar receta de favoritos
     */
    suspend fun quitarFavorito(uid: String, recetaId: String): Result<Unit> {
        return try {
            val userRef = db.collection(FirebaseConstants.COLLECTION_USUARIOS).document(uid)
            val documento = userRef.get().await()
            val favoritosActuales = documento.toObject(Usuario::class.java)?.recetasFavoritas ?: emptyList()
            
            val nuevosFavoritos = favoritosActuales.filter { it != recetaId }
            userRef.update(FirebaseConstants.FIELD_RECETAS_FAVORITAS, nuevosFavoritos).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener lista de IDs de recetas favoritas
     */
    suspend fun obtenerFavoritos(uid: String): Result<List<String>> {
        return try {
            val documento = db.collection(FirebaseConstants.COLLECTION_USUARIOS)
                .document(uid)
                .get()
                .await()
            val favoritos = documento.toObject(Usuario::class.java)?.recetasFavoritas ?: emptyList()
            Result.success(favoritos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verificar si una receta está en favoritos
     */
    suspend fun esFavorito(uid: String, recetaId: String): Result<Boolean> {
        return try {
            val resultado = obtenerFavoritos(uid)
            if (resultado.isSuccess) {
                val favoritos = resultado.getOrNull() ?: emptyList()
                Result.success(favoritos.contains(recetaId))
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
