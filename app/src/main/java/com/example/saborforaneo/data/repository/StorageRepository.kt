package com.example.saborforaneo.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Referencias de carpetas
    companion object {
        const val FOLDER_USUARIOS = "usuarios"
        const val FOLDER_RECETAS = "recetas"
        const val FOLDER_TEMP = "temp"
    }

    // ==================== FOTOS DE PERFIL ====================

    /**
     * Subir foto de perfil del usuario
     * @param imageUri URI de la imagen a subir
     * @return URL de descarga de la imagen subida
     */
    suspend fun subirFotoPerfil(imageUri: Uri): Result<String> {
        val userId = auth.currentUser?.uid 
            ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            // Crear referencia con nombre único
            val fileName = "perfil_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference
                .child("$FOLDER_USUARIOS/$userId/perfil/$fileName")
            
            // Subir imagen
            val uploadTask = storageRef.putFile(imageUri).await()
            
            // Obtener URL de descarga
            val downloadUrl = storageRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Eliminar foto de perfil anterior
     * @param imageUrl URL de la imagen a eliminar
     */
    suspend fun eliminarFotoPerfil(imageUrl: String): Result<Unit> {
        return try {
            if (imageUrl.isNotEmpty() && imageUrl.contains("firebase")) {
                val storageRef = storage.getReferenceFromUrl(imageUrl)
                storageRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // No fallar si la imagen no existe
            Result.success(Unit)
        }
    }

    // ==================== IMÁGENES DE RECETAS ====================

    /**
     * Subir imagen de receta
     * @param imageUri URI de la imagen a subir
     * @param recetaId ID de la receta (opcional, se genera uno si no se proporciona)
     * @return Par con el ID de la receta y la URL de descarga
     */
    suspend fun subirImagenReceta(
        imageUri: Uri, 
        recetaId: String? = null
    ): Result<Pair<String, String>> {
        val userId = auth.currentUser?.uid 
            ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val recetaIdFinal = recetaId ?: UUID.randomUUID().toString()
            val fileName = "receta_${System.currentTimeMillis()}.jpg"
            
            val storageRef = storage.reference
                .child("$FOLDER_RECETAS/$recetaIdFinal/$fileName")
            
            // Subir imagen
            storageRef.putFile(imageUri).await()
            
            // Obtener URL de descarga
            val downloadUrl = storageRef.downloadUrl.await()
            
            Result.success(Pair(recetaIdFinal, downloadUrl.toString()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Eliminar imagen de receta
     * @param imageUrl URL de la imagen a eliminar
     */
    suspend fun eliminarImagenReceta(imageUrl: String): Result<Unit> {
        return try {
            if (imageUrl.isNotEmpty() && imageUrl.contains("firebase")) {
                val storageRef = storage.getReferenceFromUrl(imageUrl)
                storageRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    /**
     * Eliminar todas las imágenes de una receta
     * @param recetaId ID de la receta
     */
    suspend fun eliminarImagenesReceta(recetaId: String): Result<Unit> {
        return try {
            val storageRef = storage.reference.child("$FOLDER_RECETAS/$recetaId")
            
            // Listar todos los archivos en la carpeta
            val listResult = storageRef.listAll().await()
            
            // Eliminar cada archivo
            listResult.items.forEach { item ->
                item.delete().await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== ARCHIVOS TEMPORALES ====================

    /**
     * Subir archivo temporal
     * @param fileUri URI del archivo
     * @return URL de descarga
     */
    suspend fun subirArchivoTemporal(fileUri: Uri): Result<String> {
        val userId = auth.currentUser?.uid 
            ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val fileName = "temp_${System.currentTimeMillis()}_${UUID.randomUUID()}"
            val storageRef = storage.reference
                .child("$FOLDER_TEMP/$userId/$fileName")
            
            storageRef.putFile(fileUri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Limpiar archivos temporales del usuario
     */
    suspend fun limpiarArchivosTemporal(): Result<Unit> {
        val userId = auth.currentUser?.uid 
            ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val storageRef = storage.reference.child("$FOLDER_TEMP/$userId")
            val listResult = storageRef.listAll().await()
            
            listResult.items.forEach { item ->
                item.delete().await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== UTILIDADES ====================

    /**
     * Obtener tamaño de un archivo en Storage
     * @param fileUrl URL del archivo
     * @return Tamaño en bytes
     */
    suspend fun obtenerTamanoArchivo(fileUrl: String): Result<Long> {
        return try {
            val storageRef = storage.getReferenceFromUrl(fileUrl)
            val metadata = storageRef.metadata.await()
            Result.success(metadata.sizeBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verificar si un archivo existe
     * @param fileUrl URL del archivo
     * @return true si existe, false si no
     */
    suspend fun existeArchivo(fileUrl: String): Boolean {
        return try {
            if (fileUrl.isEmpty() || !fileUrl.contains("firebase")) {
                return false
            }
            val storageRef = storage.getReferenceFromUrl(fileUrl)
            storageRef.metadata.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtener metadata de un archivo
     * @param fileUrl URL del archivo
     * @return Map con metadata (contentType, size, timeCreated, etc.)
     */
    suspend fun obtenerMetadata(fileUrl: String): Result<Map<String, Any>> {
        return try {
            val storageRef = storage.getReferenceFromUrl(fileUrl)
            val metadata = storageRef.metadata.await()
            
            val metadataMap = mapOf(
                "contentType" to (metadata.contentType ?: "unknown"),
                "size" to metadata.sizeBytes,
                "timeCreated" to (metadata.creationTimeMillis),
                "updated" to (metadata.updatedTimeMillis),
                "name" to (metadata.name ?: "unknown"),
                "path" to (metadata.path ?: "unknown")
            )
            
            Result.success(metadataMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
