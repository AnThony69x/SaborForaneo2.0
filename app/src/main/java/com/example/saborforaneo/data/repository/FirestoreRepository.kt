package com.example.saborforaneo.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val fotoPerfil: String = "",
    val rol: String = "usuario", // "usuario" o "admin"
    val fechaCreacion: Long = System.currentTimeMillis(),
    val recetasFavoritas: List<String> = emptyList(),
    val notificacionesActivas: Boolean = true,
    val ubicacionActiva: Boolean = false,
    val temaOscuro: Boolean = false,
    val temaColor: String = "VERDE" // VERDE, ROJO, AZUL, NARANJA, MORADO
)

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageRepository = StorageRepository()

    // Colecciones
    companion object {
        const val COLLECTION_USUARIOS = "usuarios"
        const val COLLECTION_RECETAS = "recetas"
        const val COLLECTION_FAVORITOS = "favoritos"
    }

    // ==================== USUARIOS ====================

    /**
     * Crear o actualizar perfil de usuario en Firestore
     */
    suspend fun crearPerfilUsuario(usuario: Usuario): Result<Unit> {
        return try {
            db.collection(COLLECTION_USUARIOS)
                .document(usuario.uid)
                .set(usuario, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener perfil de usuario por UID
     */
    suspend fun obtenerPerfilUsuario(uid: String): Result<Usuario?> {
        return try {
            val document = db.collection(COLLECTION_USUARIOS)
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
     * Obtener perfil del usuario actual
     */
    suspend fun obtenerPerfilUsuarioActual(): Result<Usuario?> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        return obtenerPerfilUsuario(uid)
    }

    /**
     * Actualizar campo específico del perfil
     */
    suspend fun actualizarCampoUsuario(uid: String, campo: String, valor: Any): Result<Unit> {
        return try {
            db.collection(COLLECTION_USUARIOS)
                .document(uid)
                .update(campo, valor)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observar cambios en tiempo real del perfil de usuario
     */
    fun observarPerfilUsuario(uid: String): Flow<Usuario?> = callbackFlow {
        val listener = db.collection(COLLECTION_USUARIOS)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val usuario = snapshot?.toObject(Usuario::class.java)
                trySend(usuario)
            }

        awaitClose { listener.remove() }
    }

    // ==================== FAVORITOS ====================

    /**
     * Agregar receta a favoritos
     */
    suspend fun agregarFavorito(recetaId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val userRef = db.collection(COLLECTION_USUARIOS).document(uid)
            
            // Obtener favoritos actuales
            val documento = userRef.get().await()
            val favoritosActuales = documento.toObject(Usuario::class.java)?.recetasFavoritas ?: emptyList()
            
            // Agregar nuevo favorito si no existe
            if (!favoritosActuales.contains(recetaId)) {
                val nuevosFavoritos = favoritosActuales + recetaId
                userRef.update("recetasFavoritas", nuevosFavoritos).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Quitar receta de favoritos
     */
    suspend fun quitarFavorito(recetaId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val userRef = db.collection(COLLECTION_USUARIOS).document(uid)
            
            // Obtener favoritos actuales
            val documento = userRef.get().await()
            val favoritosActuales = documento.toObject(Usuario::class.java)?.recetasFavoritas ?: emptyList()
            
            // Quitar favorito
            val nuevosFavoritos = favoritosActuales.filter { it != recetaId }
            userRef.update("recetasFavoritas", nuevosFavoritos).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verificar si una receta está en favoritos
     */
    suspend fun esFavorito(recetaId: String): Result<Boolean> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val documento = db.collection(COLLECTION_USUARIOS).document(uid).get().await()
            val favoritos = documento.toObject(Usuario::class.java)?.recetasFavoritas ?: emptyList()
            Result.success(favoritos.contains(recetaId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener todas las recetas favoritas
     */
    suspend fun obtenerFavoritos(): Result<List<String>> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val documento = db.collection(COLLECTION_USUARIOS).document(uid).get().await()
            val favoritos = documento.toObject(Usuario::class.java)?.recetasFavoritas ?: emptyList()
            Result.success(favoritos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== CONFIGURACIÓN ====================

    /**
     * Actualizar preferencia de notificaciones
     */
    suspend fun actualizarNotificaciones(activo: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        return actualizarCampoUsuario(uid, "notificacionesActivas", activo)
    }

    /**
     * Actualizar preferencia de ubicación
     */
    suspend fun actualizarUbicacion(activo: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        return actualizarCampoUsuario(uid, "ubicacionActiva", activo)
    }

    /**
     * Actualizar tema oscuro
     */
    suspend fun actualizarTemaOscuro(activo: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        return actualizarCampoUsuario(uid, "temaOscuro", activo)
    }

    // ==================== FOTO DE PERFIL ====================

    /**
     * Actualizar foto de perfil del usuario
     * @param imageUri URI de la nueva imagen
     * @return URL de la imagen subida
     */
    suspend fun actualizarFotoPerfil(imageUri: Uri): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            // Obtener foto de perfil anterior para eliminarla
            val perfilActual = obtenerPerfilUsuario(uid).getOrNull()
            val fotoAnterior = perfilActual?.fotoPerfil ?: ""
            
            // Subir nueva foto
            val resultSubida = storageRepository.subirFotoPerfil(imageUri)
            
            if (resultSubida.isSuccess) {
                val nuevaUrl = resultSubida.getOrNull()!!
                
                // Actualizar URL en Firestore
                actualizarCampoUsuario(uid, "fotoPerfil", nuevaUrl)
                
                // Eliminar foto anterior si existe
                if (fotoAnterior.isNotEmpty()) {
                    storageRepository.eliminarFotoPerfil(fotoAnterior)
                }
                
                Result.success(nuevaUrl)
            } else {
                resultSubida
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Eliminar foto de perfil del usuario
     */
    suspend fun eliminarFotoPerfil(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            // Obtener foto actual
            val perfilActual = obtenerPerfilUsuario(uid).getOrNull()
            val fotoActual = perfilActual?.fotoPerfil ?: ""
            
            // Eliminar de Storage
            if (fotoActual.isNotEmpty()) {
                storageRepository.eliminarFotoPerfil(fotoActual)
            }
            
            // Actualizar Firestore
            actualizarCampoUsuario(uid, "fotoPerfil", "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
