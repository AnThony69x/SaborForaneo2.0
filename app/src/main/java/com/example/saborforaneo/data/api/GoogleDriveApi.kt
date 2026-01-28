package com.example.saborforaneo.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API REST de Google Drive
 * Documentación: https://developers.google.com/drive/api/v3/reference
 */
interface GoogleDriveApi {

    companion object {
        const val BASE_URL = "https://www.googleapis.com/"
        const val UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/"
    }

    /**
     * Lista archivos en Google Drive
     */
    @GET("drive/v3/files")
    suspend fun listFiles(
        @Header("Authorization") authorization: String,
        @Query("q") query: String? = null,
        @Query("spaces") spaces: String = "drive",
        @Query("fields") fields: String = "files(id,name,createdTime,size,mimeType)",
        @Query("orderBy") orderBy: String = "createdTime desc"
    ): Response<DriveFileList>

    /**
     * Crea una carpeta en Google Drive
     */
    @POST("drive/v3/files")
    suspend fun createFolder(
        @Header("Authorization") authorization: String,
        @Body metadata: DriveFileMetadata
    ): Response<DriveFile>

    /**
     * Sube un archivo a Google Drive (multipart)
     */
    @Multipart
    @POST("upload/drive/v3/files?uploadType=multipart")
    suspend fun uploadFile(
        @Header("Authorization") authorization: String,
        @Part metadata: MultipartBody.Part,
        @Part file: MultipartBody.Part
    ): Response<DriveFile>

    /**
     * Descarga un archivo de Google Drive
     */
    @GET("drive/v3/files/{fileId}")
    @Streaming
    suspend fun downloadFile(
        @Header("Authorization") authorization: String,
        @Path("fileId") fileId: String,
        @Query("alt") alt: String = "media"
    ): Response<ResponseBody>

    /**
     * Elimina un archivo de Google Drive
     */
    @DELETE("drive/v3/files/{fileId}")
    suspend fun deleteFile(
        @Header("Authorization") authorization: String,
        @Path("fileId") fileId: String
    ): Response<Unit>
}

/**
 * Modelos de datos para la API de Google Drive
 */
data class DriveFileList(
    val files: List<DriveFile> = emptyList()
)

data class DriveFile(
    val id: String? = null,
    val name: String? = null,
    val mimeType: String? = null,
    val createdTime: String? = null,
    val size: String? = null,
    val parents: List<String>? = null
)

data class DriveFileMetadata(
    val name: String,
    val mimeType: String,
    val parents: List<String>? = null
)
