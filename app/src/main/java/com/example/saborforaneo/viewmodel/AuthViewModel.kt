    package com.example.saborforaneo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.repository.FirestoreRepository
import com.example.saborforaneo.data.repository.Usuario
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.EmailAuthProvider
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
    data class NecesitaContrasena(val email: String, val nombre: String, val idToken: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreRepository = FirestoreRepository()

    // GoogleSignInClient para cerrar sesión de Google
    private var googleSignInClient: GoogleSignInClient? = null

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

    /**
     * Establecer el GoogleSignInClient para poder cerrar sesión de Google
     */
    fun setGoogleSignInClient(client: GoogleSignInClient) {
        googleSignInClient = client
    }

    private fun checkAuthStatus() {
        val usuarioActual = auth.currentUser
        _currentUser.value = usuarioActual

        if (usuarioActual != null) {
            // Solo verificar el rol si hay un usuario autenticado
            _authState.value = AuthState.Success(usuarioActual)
            viewModelScope.launch {
                verificarRolUsuario()
            }
        } else {
            // No hay usuario autenticado, resetear todo
            _authState.value = AuthState.Idle
            _esAdmin.value = false
            _usuarioFirestore.value = null
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

    /**
     * Iniciar sesión con Google usando el token de ID
     */
    fun iniciarSesionConGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Crear credencial de Google
                val credential = GoogleAuthProvider.getCredential(idToken, null)

                // Iniciar sesión con Firebase
                val result = auth.signInWithCredential(credential).await()

                result.user?.let { user ->
                    // Verificar si el usuario ya existe en Firestore
                    val existeUsuario = firestoreRepository.obtenerPerfilUsuario(user.uid)

                    if (existeUsuario.isFailure || existeUsuario.getOrNull() == null) {
                        // Es un usuario nuevo, solicitar contraseña
                        // Cerrar la sesión temporal de Google
                        auth.signOut()
                        _authState.value = AuthState.NecesitaContrasena(
                            email = user.email ?: "",
                            nombre = user.displayName ?: "Usuario",
                            idToken = idToken
                        )
                    } else {
                        // Usuario existente, verificar rol y continuar
                        _currentUser.value = result.user
                        verificarRolUsuario()
                        _authState.value = AuthState.Success(result.user)
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(manejarErrorAuth(e))
            }
        }
    }

    /**
     * Completar el registro con Google estableciendo una contraseña
     */
    fun completarRegistroConGoogle(email: String, nombre: String, password: String, idToken: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Crear credencial de Google
                val credential = GoogleAuthProvider.getCredential(idToken, null)

                // Iniciar sesión con Firebase usando Google
                val result = auth.signInWithCredential(credential).await()

                result.user?.let { user ->
                    // Actualizar la contraseña del usuario en Firebase Auth
                    // Nota: No se puede establecer una contraseña directamente para usuarios de Google
                    // En su lugar, vincularemos el email/password como método adicional

                    // Determinar el rol del usuario
                    val rol = if (esEmailAdmin(email)) "admin" else "usuario"

                    // Crear perfil en Firestore
                    val nuevoUsuario = Usuario(
                        uid = user.uid,
                        nombre = nombre,
                        email = email,
                        rol = rol,
                        fechaCreacion = System.currentTimeMillis()
                    )

                    firestoreRepository.crearPerfilUsuario(nuevoUsuario)
                    _usuarioFirestore.value = nuevoUsuario
                    _esAdmin.value = (rol == "admin")

                    // Intentar vincular email/password como método adicional
                    try {
                        val emailCredential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
                        user.linkWithCredential(emailCredential).await()
                    } catch (e: Exception) {
                        // Si falla la vinculación (por ejemplo, si el email ya tiene contraseña)
                        // simplemente continuamos. El usuario ya está autenticado con Google.
                        println("No se pudo vincular email/password: ${e.message}")
                    }

                    _currentUser.value = result.user
                    _authState.value = AuthState.Success(result.user)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(manejarErrorAuth(e))
            }
        }
    }

    fun cerrarSesion() {
        // Cerrar sesión de Firebase
        auth.signOut()

        // Cerrar sesión de Google Sign-In (esto limpiará la cuenta seleccionada)
        googleSignInClient?.signOut()

        // Limpiar estados
        _currentUser.value = null
        _authState.value = AuthState.Idle
        _esAdmin.value = false
        _usuarioFirestore.value = null
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
        // Solo resetear el estado, no cerrar sesión
        _authState.value = AuthState.Idle
    }

    fun limpiarTodoElEstado() {
        // Limpiar completamente todo el estado (para cuando se va a la pantalla de login)
        _authState.value = AuthState.Idle
        // NO limpiar _currentUser ni _esAdmin aquí porque puede haber una sesión válida
    }

    /**
     * Maneja los errores de Firebase Authentication
     * y devuelve mensajes claros para el usuario
     */
    private fun manejarErrorAuth(exception: Throwable): String {
        val mensaje = exception.message?.lowercase() ?: ""
        
        return when {
            // Errores de autenticación - ESTOS SON LOS MÁS COMUNES
            mensaje.contains("invalid-credential") ||
            mensaje.contains("wrong-password") ||
            mensaje.contains("user-not-found") ||
            mensaje.contains("invalid-login-credentials") ||
            mensaje.contains("invalid_login_credentials") ->
                "Email o contraseña incorrectos. Verifica tus datos."

            // Errores de registro
            mensaje.contains("email-already-in-use") ||
            mensaje.contains("already in use") ->
                "Este email ya está registrado. Intenta iniciar sesión."

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
            
            // Token expirado - SOLO para sesiones ya iniciadas, no para login
            mensaje.contains("token") && mensaje.contains("expired") ->
                "Tu sesión ha caducado. Por favor, inicia sesión nuevamente."

            // Error genérico de Firebase
            mensaje.contains("firebase") -> 
                "Error del servidor. Intenta de nuevo más tarde."
            
            // Error desconocido
            else -> {
                // Log para debug (puedes ver el error real en logcat)
                println("Error de autenticación no manejado: ${exception.message}")
                "Error al iniciar sesión. Verifica tus credenciales e intenta de nuevo."
            }
        }
    }
}
