    package com.example.saborforaneo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.repository.FirestoreRepository
import com.example.saborforaneo.data.repository.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreRepository = FirestoreRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _esAdmin = MutableStateFlow(false)
    val esAdmin: StateFlow<Boolean> = _esAdmin.asStateFlow()

    private val _usuarioFirestore = MutableStateFlow<Usuario?>(null)
    val usuarioFirestore: StateFlow<Usuario?> = _usuarioFirestore.asStateFlow()

    // Email del administrador
    companion object {
        const val ADMIN_EMAIL = "saborforaneo@gmail.com"
    }

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        _currentUser.value = auth.currentUser
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success(auth.currentUser)
            viewModelScope.launch {
                verificarRolUsuario()
            }
        }
    }

    /**
     * Verificar si el usuario actual es administrador
     */
    private suspend fun verificarRolUsuario() {
        try {
            val uid = auth.currentUser?.uid ?: return
            val result = firestoreRepository.obtenerPerfilUsuario(uid)
            
            if (result.isSuccess) {
                val usuario = result.getOrNull()
                _usuarioFirestore.value = usuario
                _esAdmin.value = usuario?.rol == "admin" || usuario?.email == ADMIN_EMAIL
            } else {
                _esAdmin.value = false
            }
        } catch (e: Exception) {
            _esAdmin.value = false
        }
    }

    /**
     * Verificar si un usuario es admin por email
     */
    fun esEmailAdmin(email: String): Boolean {
        return email.lowercase() == ADMIN_EMAIL.lowercase()
    }

    fun registrarUsuario(nombre: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Crear usuario en Firebase Authentication
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                // Actualizar el perfil con el nombre
                result.user?.let { user ->
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                    user.updateProfile(profileUpdates).await()
                    
                    // Determinar el rol del usuario
                    val rol = if (esEmailAdmin(email)) "admin" else "usuario"
                    
                    // Crear perfil en Firestore
                    val usuario = Usuario(
                        uid = user.uid,
                        nombre = nombre,
                        email = email,
                        rol = rol,
                        fechaCreacion = System.currentTimeMillis()
                    )
                    firestoreRepository.crearPerfilUsuario(usuario)
                    
                    // Actualizar estado de admin
                    _esAdmin.value = (rol == "admin")
                    _usuarioFirestore.value = usuario
                    
                    // Enviar email de verificación (opcional)
                    user.sendEmailVerification().await()
                }

                _currentUser.value = result.user
                _authState.value = AuthState.Success(result.user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(manejarErrorAuth(e))
            }
        }
    }

    fun iniciarSesion(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val result = auth.signInWithEmailAndPassword(email, password).await()
                
                _currentUser.value = result.user
                
                // Verificar rol del usuario y esperar a que termine
                verificarRolUsuario()
                
                // Emitir el estado de éxito después de verificar el rol
                _authState.value = AuthState.Success(result.user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(manejarErrorAuth(e))
            }
        }
    }

    fun cerrarSesion() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun recuperarContrasena(email: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Success(null)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(manejarErrorAuth(e))
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    /**
     * Maneja los errores de Firebase Authentication
     * y devuelve mensajes claros para el usuario
     */
    private fun manejarErrorAuth(exception: Throwable): String {
        val mensaje = exception.message?.lowercase() ?: ""
        
        return when {
            // Errores de registro
            mensaje.contains("email-already-in-use") || 
            mensaje.contains("already in use") -> 
                "Este email ya está registrado. Intenta iniciar sesión."
            
            // Errores de autenticación
            mensaje.contains("invalid-credential") ||
            mensaje.contains("wrong-password") ||
            mensaje.contains("user-not-found") ||
            mensaje.contains("invalid-login-credentials") -> 
                "Email o contraseña incorrectos. Verifica tus datos."
            
            // Email inválido
            mensaje.contains("invalid-email") ||
            mensaje.contains("badly formatted") -> 
                "El formato del email no es válido."
            
            // Contraseña débil
            mensaje.contains("weak-password") ||
            mensaje.contains("password should be at least") -> 
                "La contraseña debe tener al menos 6 caracteres."
            
            // Usuario deshabilitado
            mensaje.contains("user-disabled") -> 
                "Esta cuenta ha sido deshabilitada. Contacta a soporte."
            
            // Demasiados intentos
            mensaje.contains("too-many-requests") -> 
                "Demasiados intentos. Espera unos minutos e intenta de nuevo."
            
            // Errores de red
            mensaje.contains("network") ||
            mensaje.contains("timeout") ||
            mensaje.contains("unable to resolve host") -> 
                "Sin conexión a internet. Verifica tu red y vuelve a intentar."
            
            // Operación no permitida
            mensaje.contains("operation-not-allowed") -> 
                "Este método de autenticación no está habilitado."
            
            // Email no verificado (si se usa en el futuro)
            mensaje.contains("email-not-verified") -> 
                "Por favor verifica tu email antes de continuar."
            
            // Token expirado
            mensaje.contains("expired") -> 
                "La sesión ha expirado. Vuelve a iniciar sesión."
            
            // Error genérico de Firebase
            mensaje.contains("firebase") -> 
                "Error del servidor. Intenta de nuevo más tarde."
            
            // Error desconocido
            else -> "Error inesperado. Intenta de nuevo o contacta a soporte."
        }
    }
}
