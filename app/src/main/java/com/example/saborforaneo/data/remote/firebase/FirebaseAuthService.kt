package com.example.saborforaneo.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

/**
 * Servicio para operaciones de Firebase Authentication
 * Encapsula toda la lógica de autenticación
 */
class FirebaseAuthService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Registrar nuevo usuario
     */
    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Usuario nulo después del registro"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Iniciar sesión
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Usuario nulo después del login"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar perfil del usuario (nombre, foto)
     */
    suspend fun updateProfile(displayName: String? = null, photoUrl: String? = null): Result<Unit> {
        return try {
            val user = auth.currentUser 
                ?: return Result.failure(Exception("No hay usuario autenticado"))
            
            val profileUpdates = UserProfileChangeRequest.Builder().apply {
                displayName?.let { setDisplayName(it) }
                photoUrl?.let { setPhotoUri(android.net.Uri.parse(it)) }
            }.build()
            
            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Enviar email de recuperación de contraseña
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Enviar email de verificación
     */
    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
                ?: return Result.failure(Exception("No hay usuario autenticado"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cerrar sesión
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Obtener usuario actual
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Verificar si hay usuario autenticado
     */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
}
