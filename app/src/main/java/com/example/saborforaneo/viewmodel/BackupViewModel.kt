package com.example.saborforaneo.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.api.DriveFile
import com.example.saborforaneo.data.api.DriveFileMetadata
import com.example.saborforaneo.data.api.GoogleDriveApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class BackupUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val lastBackupDate: String? = null,
    val needsDrivePermission: Boolean = false,
    val backupUrl: String? = null
)

class BackupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private var accessToken: String? = null
    private val driveApi: GoogleDriveApi

    companion object {
        private const val BACKUP_FOLDER_NAME = "SaborForaneo_Backups"
        private const val DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file"
    }

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        driveApi = Retrofit.Builder()
            .baseUrl(GoogleDriveApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleDriveApi::class.java)
    }

    /**
     * Verifica si el usuario tiene permisos de Google Drive
     */
    fun checkDrivePermission(context: Context): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && GoogleSignIn.hasPermissions(
            account,
            Scope(DRIVE_SCOPE)
        )
    }

    /**
     * Obtiene el Intent para solicitar permisos de Google Drive
     */
    fun getDriveSignInIntent(context: Context): Intent {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DRIVE_SCOPE))
            .build()

        val client = GoogleSignIn.getClient(context, signInOptions)
        return client.signInIntent
    }

    /**
     * Inicializa el servicio con el token de acceso
     */
    fun initializeDriveService(context: Context, account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                // Obtener el token de acceso
                withContext(Dispatchers.IO) {
                    val token = com.google.android.gms.auth.GoogleAuthUtil.getToken(
                        context,
                        account.account!!,
                        "oauth2:$DRIVE_SCOPE"
                    )
                    accessToken = token
                }

                _uiState.value = _uiState.value.copy(
                    needsDrivePermission = false,
                    message = "✅ Google Drive conectado"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al conectar con Google Drive: ${e.message}"
                )
            }
        }
    }

    /**
     * Realiza el respaldo completo de datos a Google Drive
     */
    fun realizarRespaldo(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)

            try {
                // Verificar permisos de Drive
                if (accessToken == null) {
                    val account = GoogleSignIn.getLastSignedInAccount(context)
                    if (account == null || !checkDrivePermission(context)) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            needsDrivePermission = true
                        )
                        return@launch
                    }

                    // Obtener token
                    withContext(Dispatchers.IO) {
                        accessToken = com.google.android.gms.auth.GoogleAuthUtil.getToken(
                            context,
                            account.account!!,
                            "oauth2:$DRIVE_SCOPE"
                        )
                    }
                }

                // Recopilar datos de Firestore
                val backupData = recopilarDatos()

                // Crear archivo de respaldo
                val backupFile = crearArchivoRespaldo(context, backupData)

                // Subir a Google Drive
                subirAGoogleDrive(backupFile)

                // Limpiar archivo temporal
                backupFile.delete()

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val fechaActual = dateFormat.format(Date())

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    message = "✅ Respaldo guardado en Google Drive",
                    lastBackupDate = fechaActual
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al realizar respaldo: ${e.message}"
                )
            }
        }
    }

    /**
     * Recopila todos los datos de Firestore para el respaldo
     */
    private suspend fun recopilarDatos(): Map<String, Any> = withContext(Dispatchers.IO) {
        val datos = mutableMapOf<String, Any>()

        try {
            // Respaldar colección de recetas
            val recetas = firestore.collection("recetas").get().await()
            datos["recetas"] = recetas.documents.map { doc ->
                mapOf("id" to doc.id, "data" to (doc.data ?: emptyMap<String, Any>()))
            }

            // Respaldar colección de usuarios
            val usuarios = firestore.collection("usuarios").get().await()
            datos["usuarios"] = usuarios.documents.map { doc ->
                mapOf("id" to doc.id, "data" to (doc.data ?: emptyMap<String, Any>()))
            }

            // Respaldar colección de recetas de la comunidad
            val recetasComunidad = firestore.collection("recetas_comunidad").get().await()
            datos["recetas_comunidad"] = recetasComunidad.documents.map { doc ->
                mapOf("id" to doc.id, "data" to (doc.data ?: emptyMap<String, Any>()))
            }

            // Respaldar colección de comentarios
            val comentarios = firestore.collection("comentarios").get().await()
            datos["comentarios"] = comentarios.documents.map { doc ->
                mapOf("id" to doc.id, "data" to (doc.data ?: emptyMap<String, Any>()))
            }

            // Metadatos del respaldo
            datos["metadata"] = mapOf(
                "fecha_respaldo" to System.currentTimeMillis(),
                "version_app" to "2.0",
                "total_recetas" to recetas.size(),
                "total_usuarios" to usuarios.size(),
                "total_recetas_comunidad" to recetasComunidad.size(),
                "total_comentarios" to comentarios.size()
            )
        } catch (e: Exception) {
            datos["error"] = "Error al recopilar datos: ${e.message}"
        }

        datos
    }

    /**
     * Crea un archivo JSON con los datos del respaldo
     */
    private suspend fun crearArchivoRespaldo(context: Context, datos: Map<String, Any>): File =
        withContext(Dispatchers.IO) {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "backup_saborforaneo_$timestamp.json"

            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { output ->
                output.write(gson.toJson(datos).toByteArray())
            }

            file
        }

    /**
     * Sube el archivo de respaldo a Google Drive usando la API REST
     */
    private suspend fun subirAGoogleDrive(file: File) = withContext(Dispatchers.IO) {
        val token = accessToken ?: throw Exception("No hay token de acceso")
        val authHeader = "Bearer $token"

        // Buscar o crear la carpeta de respaldos
        val folderId = obtenerOCrearCarpeta(authHeader)

        // Preparar metadata del archivo
        val metadata = DriveFileMetadata(
            name = file.name,
            mimeType = "application/json",
            parents = listOf(folderId)
        )

        val metadataJson = gson.toJson(metadata)
        val metadataPart = MultipartBody.Part.createFormData(
            "metadata",
            null,
            metadataJson.toRequestBody("application/json".toMediaType())
        )

        // Preparar el archivo
        val filePart = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody("application/json".toMediaType())
        )

        // Subir el archivo
        val response = driveApi.uploadFile(authHeader, metadataPart, filePart)

        if (!response.isSuccessful) {
            throw Exception("Error al subir archivo: ${response.code()} - ${response.message()}")
        }
    }

    /**
     * Obtiene o crea la carpeta de respaldos en Google Drive
     */
    private suspend fun obtenerOCrearCarpeta(authHeader: String): String {
        // Buscar carpeta existente
        val query = "name='$BACKUP_FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false"
        val listResponse = driveApi.listFiles(authHeader, query)

        if (listResponse.isSuccessful) {
            val files = listResponse.body()?.files ?: emptyList()
            if (files.isNotEmpty()) {
                return files[0].id ?: throw Exception("ID de carpeta no encontrado")
            }
        }

        // Crear nueva carpeta
        val folderMetadata = DriveFileMetadata(
            name = BACKUP_FOLDER_NAME,
            mimeType = "application/vnd.google-apps.folder"
        )

        val createResponse = driveApi.createFolder(authHeader, folderMetadata)

        if (createResponse.isSuccessful) {
            return createResponse.body()?.id ?: throw Exception("No se pudo crear la carpeta")
        } else {
            throw Exception("Error al crear carpeta: ${createResponse.message()}")
        }
    }

    /**
     * Lista los respaldos disponibles en Google Drive
     */
    fun listarRespaldos(context: Context, onResult: (List<BackupInfo>) -> Unit) {
        viewModelScope.launch {
            try {
                if (accessToken == null) {
                    val account = GoogleSignIn.getLastSignedInAccount(context)
                    if (account != null) {
                        withContext(Dispatchers.IO) {
                            accessToken = com.google.android.gms.auth.GoogleAuthUtil.getToken(
                                context,
                                account.account!!,
                                "oauth2:$DRIVE_SCOPE"
                            )
                        }
                    }
                }

                val token = accessToken ?: return@launch
                val authHeader = "Bearer $token"

                withContext(Dispatchers.IO) {
                    // Primero obtener el ID de la carpeta
                    val folderQuery = "name='$BACKUP_FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false"
                    val folderResponse = driveApi.listFiles(authHeader, folderQuery)

                    val folderId = folderResponse.body()?.files?.firstOrNull()?.id

                    if (folderId != null) {
                        // Listar archivos en la carpeta
                        val filesQuery = "'$folderId' in parents and trashed=false"
                        val filesResponse = driveApi.listFiles(authHeader, filesQuery)

                        val backups = filesResponse.body()?.files?.map { file ->
                            BackupInfo(
                                id = file.id ?: "",
                                name = file.name ?: "",
                                date = file.createdTime ?: "",
                                size = formatSize(file.size?.toLongOrNull() ?: 0)
                            )
                        } ?: emptyList()

                        withContext(Dispatchers.Main) {
                            onResult(backups)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onResult(emptyList())
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al listar respaldos: ${e.message}"
                )
                onResult(emptyList())
            }
        }
    }

    /**
     * Elimina un respaldo de Google Drive
     */
    fun eliminarRespaldo(context: Context, fileId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = accessToken ?: return@launch
                val authHeader = "Bearer $token"

                withContext(Dispatchers.IO) {
                    driveApi.deleteFile(authHeader, fileId)
                }

                _uiState.value = _uiState.value.copy(
                    message = "✅ Respaldo eliminado"
                )
                onComplete()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al eliminar: ${e.message}"
                )
            }
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null, isSuccess = false)
    }
}

data class BackupInfo(
    val id: String,
    val name: String,
    val date: String,
    val size: String
)
