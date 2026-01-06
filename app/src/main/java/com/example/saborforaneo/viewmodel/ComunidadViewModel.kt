package com.example.saborforaneo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.model.RecetaComunidad
import com.example.saborforaneo.data.repository.ComunidadRepository
import com.example.saborforaneo.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ComunidadUiState(
    val recetas: List<RecetaComunidad> = emptyList(),
    val misRecetas: List<RecetaComunidad> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val vistaActual: VistaComunidad = VistaComunidad.TODAS
)

enum class VistaComunidad {
    TODAS,
    MIS_RECETAS
}

class ComunidadViewModel : ViewModel() {
    private val comunidadRepository = ComunidadRepository()
    private val firestoreRepository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ComunidadUiState())
    val uiState: StateFlow<ComunidadUiState> = _uiState.asStateFlow()

    init {
        observarRecetasComunidad()
    }

    /**
     * Observar recetas de la comunidad en tiempo real
     */
    private fun observarRecetasComunidad() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            try {
                comunidadRepository.observarRecetasComunidad().collect { recetas ->
                    // Marcar las recetas que le gustan al usuario actual
                    val uid = auth.currentUser?.uid ?: ""
                    val recetasConLike = recetas.map { receta ->
                        receta.copy()
                    }

                    _uiState.value = _uiState.value.copy(
                        recetas = recetasConLike,
                        cargando = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "Error al cargar recetas: ${e.message}"
                )
            }
        }
    }

    /**
     * Cargar recetas del usuario actual
     */
    fun cargarMisRecetas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            try {
                val resultado = comunidadRepository.obtenerMisRecetas()
                if (resultado.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        misRecetas = resultado.getOrNull() ?: emptyList(),
                        cargando = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "Error al cargar tus recetas: ${resultado.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Crear nueva receta
     */
    fun crearReceta(
        nombre: String,
        descripcion: String,
        categoria: String,
        tiempoPreparacion: Int,
        porciones: Int,
        dificultad: String,
        ingredientes: List<String>,
        pasos: List<String>,
        esVegetariana: Boolean,
        esVegana: Boolean,
        imagenUrl: String,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: run {
                    onError("Usuario no autenticado")
                    return@launch
                }

                // Obtener info del usuario
                val usuario = firestoreRepository.obtenerPerfilUsuario(uid).getOrNull()
                if (usuario == null) {
                    onError("No se pudo obtener información del usuario")
                    return@launch
                }

                val receta = RecetaComunidad(
                    nombre = nombre,
                    descripcion = descripcion,
                    categoria = categoria,
                    tiempoPreparacion = tiempoPreparacion,
                    porciones = porciones,
                    dificultad = try {
                        com.example.saborforaneo.data.model.Dificultad.valueOf(dificultad)
                    } catch (e: Exception) {
                        com.example.saborforaneo.data.model.Dificultad.MEDIA
                    },
                    ingredientes = ingredientes,
                    pasos = pasos,
                    esVegetariana = esVegetariana,
                    esVegana = esVegana,
                    imagenUrl = imagenUrl,
                    autorUid = uid,
                    autorNombre = usuario.nombre,
                    autorFoto = usuario.fotoPerfil
                )

                val resultado = comunidadRepository.crearReceta(receta, null)
                if (resultado.isSuccess) {
                    cargarMisRecetas()
                    onSuccess()
                } else {
                    onError("Error al crear receta: ${resultado.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    /**
     * Actualizar receta existente
     */
    fun actualizarReceta(
        recetaId: String,
        nombre: String,
        descripcion: String,
        categoria: String,
        tiempoPreparacion: Int,
        porciones: Int,
        dificultad: String,
        ingredientes: List<String>,
        pasos: List<String>,
        esVegetariana: Boolean,
        esVegana: Boolean,
        imagenUrlActual: String,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: run {
                    onError("Usuario no autenticado")
                    return@launch
                }

                val usuario = firestoreRepository.obtenerPerfilUsuario(uid).getOrNull()
                if (usuario == null) {
                    onError("No se pudo obtener información del usuario")
                    return@launch
                }

                val receta = RecetaComunidad(
                    id = recetaId,
                    nombre = nombre,
                    descripcion = descripcion,
                    categoria = categoria,
                    tiempoPreparacion = tiempoPreparacion,
                    porciones = porciones,
                    dificultad = try {
                        com.example.saborforaneo.data.model.Dificultad.valueOf(dificultad)
                    } catch (e: Exception) {
                        com.example.saborforaneo.data.model.Dificultad.MEDIA
                    },
                    ingredientes = ingredientes,
                    pasos = pasos,
                    esVegetariana = esVegetariana,
                    esVegana = esVegana,
                    imagenUrl = imagenUrlActual,
                    autorUid = uid,
                    autorNombre = usuario.nombre,
                    autorFoto = usuario.fotoPerfil
                )

                val resultado = comunidadRepository.actualizarReceta(recetaId, receta, imageUri)
                if (resultado.isSuccess) {
                    cargarMisRecetas()
                    onSuccess()
                } else {
                    onError("Error al actualizar receta: ${resultado.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    /**
     * Eliminar receta
     */
    fun eliminarReceta(recetaId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val resultado = comunidadRepository.eliminarReceta(recetaId)
                if (resultado.isSuccess) {
                    cargarMisRecetas()
                    onSuccess()
                } else {
                    onError("Error al eliminar receta: ${resultado.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    /**
     * Toggle like en receta
     */
    fun toggleLike(recetaId: String) {
        viewModelScope.launch {
            try {
                comunidadRepository.toggleLike(recetaId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al dar like: ${e.message}"
                )
            }
        }
    }

    /**
     * Cambiar vista actual
     */
    fun cambiarVista(vista: VistaComunidad) {
        _uiState.value = _uiState.value.copy(vistaActual = vista)
        if (vista == VistaComunidad.MIS_RECETAS) {
            cargarMisRecetas()
        }
    }

    /**
     * Limpiar error
     */
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

