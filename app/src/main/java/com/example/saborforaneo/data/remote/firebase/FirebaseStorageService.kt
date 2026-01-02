package com.example.saborforaneo.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Servicio para operaciones con Firebase Storage
 * Maneja subida, descarga y eliminación de archivos
 */
class FirebaseStorageService {
    private val storage = FirebaseStorage.getInstance()

    /**
     * Subir imagen genérica
     * @param uri URI de la imagen local
     * @param path Ruta en Storage (ej: "usuarios/uid123/perfil")
     * @param fileName Nombre del archivo
     * @return URL de descarga
     */
    suspend fun subirImagen(uri: Uri, path: String, fileName: String): Result<String> {
        return try {
            val storageRef = storage.reference.child("$path/$fileName")
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Subir foto de perfil
     * @param uri URI de la imagen
     * @param uid ID del usuario
     * @return URL de descarga
     */
    suspend fun subirFotoPerfil(uri: Uri, uid: String): Result<String> {
        val fileName = "perfil_${System.currentTimeMillis()}.jpg"
        val path = "${FirebaseConstants.STORAGE_USUARIOS}/$uid/perfil"
        return subirImagen(uri, path, fileName)
    }

    /**
     * Subir imagen de receta
     * @param uri URI de la imagen
     * @param recetaId ID de la receta
     * @return URL de descarga
     */
    suspend fun subirImagenReceta(uri: Uri, recetaId: String): Result<String> {
        val fileName = "receta_${System.currentTimeMillis()}.jpg"
        val path = "${FirebaseConstants.STORAGE_RECETAS}/$recetaId"
        return subirImagen(uri, path, fileName)
    }

    /**
     * Eliminar imagen por URL
     * @param url URL de descarga de Firebase Storage
     */
    suspend fun eliminarImagen(url: String): Result<Unit> {
        return try {
            if (url.isEmpty() || !url.contains("firebase")) {
                return Result.success(Unit) // No es URL de Firebase
            }
            val storageRef = storage.getReferenceFromUrl(url)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            // No fallar si el archivo no existe
            Result.success(Unit)
        }
    }

    /**
     * Verificar si existe un archivo
     * @param url URL de descarga
     * @return true si existe, false si no
     */
    suspend fun existeArchivo(url: String): Boolean {
        return try {
            if (url.isEmpty() || !url.contains("firebase")) return false
            val storageRef = storage.getReferenceFromUrl(url)
            storageRef.metadata.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtener URL de descarga de un archivo
     * @param path Ruta completa en Storage
     */
    suspend fun obtenerUrlDescarga(path: String): Result<String> {
        return try {
            val storageRef = storage.reference.child(path)
            val url = storageRef.downloadUrl.await()
            Result.success(url.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
