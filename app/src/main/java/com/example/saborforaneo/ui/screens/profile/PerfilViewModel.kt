package com.example.saborforaneo.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.repository.FirestoreRepository
import com.example.saborforaneo.data.repository.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class TemaColor(val nombreMostrar: String, val colorPrimario: Long) {
    VERDE("Verde Clásico", 0xFF4CAF50),
    ROJO("Rojo Picante", 0xFFE53935),
    AZUL("Azul Océano", 0xFF6B9FBF),
    NARANJA("Naranja Tropical", 0xFFFF6F00),
    MORADO("Morado Chef", 0xFF8E24AA)
}

enum class ModoTema(val nombreMostrar: String) {
    AUTOMATICO("Automático (Sistema)"),
    CLARO("Claro"),
    OSCURO("Oscuro")
}

data class EstadoPerfil(
    val uid: String = "",
    val nombreUsuario: String = "",
    val correoUsuario: String = "",
    val fotoPerfil: String = "",
    val rol: String = "usuario",
    val temaOscuro: Boolean = false,
    val modoTema: ModoTema = ModoTema.AUTOMATICO,
    val notificacionesActivas: Boolean = true,
    val ubicacionActiva: Boolean = false,
    val temaColorSeleccionado: TemaColor = TemaColor.VERDE,
    
    // Estadísticas (por ahora mock, después se calculan)
    val recetasVistas: Int = 0,
    val recetasFavoritas: Int = 0,
    val diasRacha: Int = 0,
    val tiempoTotalCocinando: String = "0h 0min",
    val categoriaFavorita: String = "🍽️ Sin categoría",
    
    // Estados de UI
    val cargando: Boolean = true,
    val error: String? = null
)

class PerfilViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestoreRepository = FirestoreRepository()
    
    private val _estado = MutableStateFlow(EstadoPerfil())
    val estado: StateFlow<EstadoPerfil> = _estado.asStateFlow()

    // Listener para detectar cambios de autenticación
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Usuario inició sesión, cargar su perfil
            cargarPerfilUsuario()
        } else {
            // Usuario cerró sesión, limpiar estado
            limpiarEstado()
        }
    }

    init {
        // Agregar listener de autenticación
        auth.addAuthStateListener(authStateListener)
        
        // Cargar perfil inicial si hay usuario
        if (auth.currentUser != null) {
            cargarPerfilUsuario()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remover listener al destruir el ViewModel
        auth.removeAuthStateListener(authStateListener)
    }

    /**
     * Cargar perfil del usuario autenticado desde Firestore
     */
    fun cargarPerfilUsuario() {
        viewModelScope.launch {
            try {
                _estado.value = _estado.value.copy(cargando = true, error = null)
                
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        error = "No hay usuario autenticado"
                    )
                    return@launch
                }

                // Cargar datos de Firestore
                val resultado = firestoreRepository.obtenerPerfilUsuario(uid)
                
                if (resultado.isSuccess) {
                    val usuario = resultado.getOrNull()
                    if (usuario != null) {
                        _estado.value = EstadoPerfil(
                            uid = usuario.uid,
                            nombreUsuario = usuario.nombre,
                            correoUsuario = usuario.email,
                            fotoPerfil = usuario.fotoPerfil,
                            rol = usuario.rol,
                            temaOscuro = usuario.temaOscuro,
                            modoTema = try {
                                ModoTema.valueOf(usuario.modoTema)
                            } catch (e: Exception) {
                                ModoTema.AUTOMATICO
                            },
                            notificacionesActivas = usuario.notificacionesActivas,
                            ubicacionActiva = usuario.ubicacionActiva,
                            temaColorSeleccionado = try {
                                TemaColor.valueOf(usuario.temaColor)
                            } catch (e: Exception) {
                                TemaColor.VERDE
                            },
                            recetasFavoritas = usuario.recetasFavoritas.size,
                            cargando = false,
                            error = null
                        )
                    } else {
                        _estado.value = _estado.value.copy(
                            cargando = false,
                            error = "No se encontró el perfil del usuario"
                        )
                    }
                } else {
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        error = "Error al cargar el perfil: ${resultado.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(
                    cargando = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Actualizar nombre, email y opcionalmente contraseña del usuario
     */
    fun actualizarPerfil(nuevoNombre: String, nuevoEmail: String, nuevaContrasena: String? = null, alCompletarse: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid
                val user = auth.currentUser
                if (uid == null || user == null) {
                    alCompletarse(false, "No hay usuario autenticado")
                    return@launch
                }

                // Actualizar nombre en Firebase Auth
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nuevoNombre)
                    .build()
                
                user.updateProfile(profileUpdates).await()

                // Actualizar contraseña si se proporcionó
                if (!nuevaContrasena.isNullOrEmpty()) {
                    user.updatePassword(nuevaContrasena).await()
                }

                // Actualizar nombre en Firestore
                val resultadoNombre = firestoreRepository.actualizarCampoUsuario(uid, "nombre", nuevoNombre)
                
                if (resultadoNombre.isSuccess) {
                    _estado.value = _estado.value.copy(
                        nombreUsuario = nuevoNombre,
                        correoUsuario = nuevoEmail
                    )
                    val mensaje = if (!nuevaContrasena.isNullOrEmpty()) {
                        "Perfil y contraseña actualizados correctamente"
                    } else {
                        "Perfil actualizado correctamente"
                    }
                    alCompletarse(true, mensaje)
                } else {
                    alCompletarse(false, "Error al actualizar: ${resultadoNombre.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                val mensajeError = when {
                    e.message?.contains("requires-recent-login") == true -> 
                        "Por seguridad, debes cerrar sesión y volver a iniciar para cambiar la contraseña"
                    else -> "Error: ${e.message}"
                }
                alCompletarse(false, mensajeError)
            }
        }
    }

    /**
     * Actualizar foto de perfil
     */
    fun actualizarFotoPerfil(imageUri: Uri, alCompletarse: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val resultado = firestoreRepository.actualizarFotoPerfil(imageUri)
                
                if (resultado.isSuccess) {
                    val nuevaUrl = resultado.getOrNull() ?: ""
                    _estado.value = _estado.value.copy(fotoPerfil = nuevaUrl)
                    alCompletarse(true, "Foto de perfil actualizada")
                } else {
                    alCompletarse(false, "Error al subir la foto: ${resultado.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                alCompletarse(false, "Error: ${e.message}")
            }
        }
    }

    /**
     * Eliminar foto de perfil
     */
    fun eliminarFotoPerfil(alCompletarse: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val resultado = firestoreRepository.eliminarFotoPerfil()
                
                if (resultado.isSuccess) {
                    _estado.value = _estado.value.copy(fotoPerfil = "")
                    alCompletarse(true, "Foto de perfil eliminada")
                } else {
                    alCompletarse(false, "Error al eliminar la foto: ${resultado.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                alCompletarse(false, "Error: ${e.message}")
            }
        }
    }

    /**
     * Cambiar tema oscuro
     */
    fun cambiarTemaOscuro(activado: Boolean) {
        viewModelScope.launch {
            val resultado = firestoreRepository.actualizarTemaOscuro(activado)
            if (resultado.isSuccess) {
                _estado.value = _estado.value.copy(temaOscuro = activado)
            }
        }
    }

    /**
     * Cambiar notificaciones
     */
    fun cambiarNotificacionesActivas(activado: Boolean) {
        viewModelScope.launch {
            val resultado = firestoreRepository.actualizarNotificaciones(activado)
            if (resultado.isSuccess) {
                _estado.value = _estado.value.copy(notificacionesActivas = activado)
            }
        }
    }

    /**
     * Cambiar ubicación
     */
    fun cambiarUbicacionActiva(activado: Boolean) {
        viewModelScope.launch {
            val resultado = firestoreRepository.actualizarUbicacion(activado)
            if (resultado.isSuccess) {
                _estado.value = _estado.value.copy(ubicacionActiva = activado)
            }
        }
    }

    /**
     * Cambiar tema de color
     */
    fun cambiarTemaColor(tema: TemaColor) {
        viewModelScope.launch {
            val resultado = firestoreRepository.actualizarCampoUsuario(
                auth.currentUser?.uid ?: return@launch,
                "temaColor",
                tema.name
            )
            if (resultado.isSuccess) {
                _estado.value = _estado.value.copy(temaColorSeleccionado = tema)
            }
        }
    }

    /**
     * Cambiar modo de tema (Automático, Claro, Oscuro)
     */
    fun cambiarModoTema(modo: ModoTema) {
        viewModelScope.launch {
            val resultado = firestoreRepository.actualizarModoTema(modo.name)
            if (resultado.isSuccess) {
                _estado.value = _estado.value.copy(modoTema = modo)
            }
        }
    }

    /**
     * Limpiar completamente el estado del perfil
     * Se llama automáticamente por el AuthStateListener al cerrar sesión
     */
    private fun limpiarEstado() {
        _estado.value = EstadoPerfil(
            uid = "",
            nombreUsuario = "",
            correoUsuario = "",
            fotoPerfil = "",
            rol = "usuario",
            temaOscuro = false,
            notificacionesActivas = true,
            ubicacionActiva = false,
            temaColorSeleccionado = TemaColor.VERDE,
            recetasVistas = 0,
            recetasFavoritas = 0,
            diasRacha = 0,
            tiempoTotalCocinando = "0h 0min",
            categoriaFavorita = "🍽️ Sin categoría",
            cargando = false,
            error = null
        )
    }
}