package com.example.saborforaneo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.model.ComentarioReceta
import com.example.saborforaneo.data.model.RecetaComunidad
import com.example.saborforaneo.data.repository.ComunidadRepository
import com.example.saborforaneo.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleRecetaComunidadUiState(
    val receta: RecetaComunidad? = null,
    val comentarios: List<ComentarioReceta> = emptyList(),
    val respuestasPorComentario: Map<String, List<ComentarioReceta>> = emptyMap(),
    val comentarioExpandido: String? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val scrollToComments: Boolean = false // Flag para hacer scroll a comentarios
)

class DetalleRecetaComunidadViewModel : ViewModel() {
    private val comunidadRepository = ComunidadRepository()
    private val firestoreRepository = FirestoreRepository()

    private val _uiState = MutableStateFlow(DetalleRecetaComunidadUiState())
    val uiState: StateFlow<DetalleRecetaComunidadUiState> = _uiState.asStateFlow()

    /**
     * Cargar receta
     */
    fun cargarReceta(recetaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            try {
                val resultado = comunidadRepository.obtenerRecetaDetalle(recetaId)
                if (resultado.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        receta = resultado.getOrNull(),
                        cargando = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "Error al cargar la receta: ${resultado.exceptionOrNull()?.message}"
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
     * Observar comentarios principales en tiempo real
     */
    fun observarComentarios(recetaId: String) {
        viewModelScope.launch {
            try {
                comunidadRepository.observarComentarios(recetaId).collect { comentarios ->
                    _uiState.value = _uiState.value.copy(
                        comentarios = comentarios,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al observar comentarios: ${e.message}"
                )
            }
        }
    }

    /**
     * Cargar comentarios principales (versión legacy)
     */
    @Suppress("unused")
    fun cargarComentarios(recetaId: String) {
        viewModelScope.launch {
            try {
                val resultado = comunidadRepository.obtenerComentarios(recetaId)
                if (resultado.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        comentarios = resultado.getOrNull() ?: emptyList(),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al cargar comentarios: ${resultado.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Cargar respuestas de un comentario
     * Siempre recarga las respuestas para tener datos actualizados
     */
    fun cargarRespuestas(comentarioId: String) {
        viewModelScope.launch {
            try {
                // Toggle expandir/colapsar
                if (_uiState.value.comentarioExpandido == comentarioId) {
                    // Si ya está expandido, colapsar
                    _uiState.value = _uiState.value.copy(comentarioExpandido = null)
                    return@launch
                }

                // Expandir y cargar respuestas
                _uiState.value = _uiState.value.copy(comentarioExpandido = comentarioId)

                val resultado = comunidadRepository.obtenerRespuestas(comentarioId)
                if (resultado.isSuccess) {
                    val respuestas = resultado.getOrNull() ?: emptyList()
                    val respuestasActualizadas = _uiState.value.respuestasPorComentario.toMutableMap()
                    respuestasActualizadas[comentarioId] = respuestas

                    _uiState.value = _uiState.value.copy(
                        respuestasPorComentario = respuestasActualizadas,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al cargar respuestas: ${resultado.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Agregar comentario principal
     * Actualización optimista: muestra inmediatamente en UI
     */
    fun agregarComentario(recetaId: String, comentario: String) {
        viewModelScope.launch {
            try {
                // Obtener info del usuario actual
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    _uiState.value = _uiState.value.copy(error = "Usuario no autenticado")
                    return@launch
                }

                // Obtener info del usuario desde Firestore
                val perfilResult = firestoreRepository.obtenerPerfilUsuario(uid)
                val perfil = perfilResult.getOrNull()

                // Crear comentario temporal para mostrar inmediatamente
                val comentarioTemporal = ComentarioReceta(
                    id = "temp_${System.currentTimeMillis()}", // ID temporal
                    recetaId = recetaId,
                    autorUid = uid,
                    autorNombre = perfil?.nombre ?: "Usuario",
                    autorFoto = perfil?.fotoPerfil ?: "",
                    comentario = comentario,
                    fechaCreacion = System.currentTimeMillis(),
                    parentId = "",
                    respuestas = 0
                )

                // Actualizar UI INMEDIATAMENTE (optimista)
                val comentariosActuales = _uiState.value.comentarios.toMutableList()
                comentariosActuales.add(0, comentarioTemporal) // Agregar al inicio
                _uiState.value = _uiState.value.copy(comentarios = comentariosActuales)

                // Guardar en Firestore (en segundo plano)
                val resultado = comunidadRepository.agregarComentario(recetaId, comentario)
                if (resultado.isSuccess) {
                    // El observer actualizará automáticamente con el comentario real
                    // Delay breve para que Firestore actualice el contador
                    kotlinx.coroutines.delay(500)
                    // Recargamos la receta para actualizar el contador de comentarios
                    cargarReceta(recetaId)
                } else {
                    // Si falla, revertir (quitar el comentario temporal)
                    _uiState.value = _uiState.value.copy(
                        comentarios = _uiState.value.comentarios.filter { it.id != comentarioTemporal.id },
                        error = "Error al agregar comentario: ${resultado.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Agregar respuesta a un comentario
     * Expande el comentario padre y recarga las respuestas
     */
    fun agregarRespuesta(recetaId: String, respuesta: String, parentId: String) {
        viewModelScope.launch {
            try {
                val resultado = comunidadRepository.agregarComentario(recetaId, respuesta, parentId)
                if (resultado.isSuccess) {
                    // Expandir el comentario padre si no está expandido
                    _uiState.value = _uiState.value.copy(comentarioExpandido = parentId)

                    // Delay para que Firestore procese
                    kotlinx.coroutines.delay(500)

                    // Recargar respuestas del comentario padre
                    val resultadoRespuestas = comunidadRepository.obtenerRespuestas(parentId)
                    if (resultadoRespuestas.isSuccess) {
                        val respuestas = resultadoRespuestas.getOrNull() ?: emptyList()
                        val respuestasActualizadas = _uiState.value.respuestasPorComentario.toMutableMap()
                        respuestasActualizadas[parentId] = respuestas

                        _uiState.value = _uiState.value.copy(
                            respuestasPorComentario = respuestasActualizadas
                        )
                    }

                    // Recargar comentarios principales para actualizar contador
                    val resultadoComentarios = comunidadRepository.obtenerComentarios(recetaId)
                    if (resultadoComentarios.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            comentarios = resultadoComentarios.getOrNull() ?: emptyList()
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al agregar respuesta: ${resultado.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Eliminar comentario o respuesta
     * Actualización optimista: remueve inmediatamente de UI
     */
    fun eliminarComentario(comentarioId: String, recetaId: String) {
        viewModelScope.launch {
            try {
                // Guardar el estado actual por si necesitamos revertir
                val comentariosOriginales = _uiState.value.comentarios

                // Actualizar UI INMEDIATAMENTE (optimista) - remover comentario
                val comentariosActualizados = _uiState.value.comentarios.filter { it.id != comentarioId }
                _uiState.value = _uiState.value.copy(comentarios = comentariosActualizados)

                // Eliminar de Firestore (en segundo plano)
                val resultado = comunidadRepository.eliminarComentario(comentarioId, recetaId)
                if (resultado.isSuccess) {
                    // El observer confirmará la eliminación
                    // Recargamos la receta para actualizar contador
                    kotlinx.coroutines.delay(500)
                    cargarReceta(recetaId)

                    // Si es un comentario expandido, recargar sus respuestas
                    _uiState.value.comentarioExpandido?.let { expandidoId ->
                        cargarRespuestas(expandidoId)
                    }
                } else {
                    // Si falla, revertir (restaurar comentario)
                    _uiState.value = _uiState.value.copy(
                        comentarios = comentariosOriginales,
                        error = "Error al eliminar: ${resultado.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Editar comentario o respuesta
     */
    fun editarComentario(comentarioId: String, nuevoTexto: String, recetaId: String, parentId: String = "") {
        viewModelScope.launch {
            try {
                val resultado = comunidadRepository.editarComentario(comentarioId, nuevoTexto)
                if (resultado.isSuccess) {
                    // Si es una respuesta, recargar respuestas del padre
                    if (parentId.isNotEmpty()) {
                        val resultadoRespuestas = comunidadRepository.obtenerRespuestas(parentId)
                        if (resultadoRespuestas.isSuccess) {
                            val respuestas = resultadoRespuestas.getOrNull() ?: emptyList()
                            val respuestasActualizadas = _uiState.value.respuestasPorComentario.toMutableMap()
                            respuestasActualizadas[parentId] = respuestas

                            _uiState.value = _uiState.value.copy(
                                respuestasPorComentario = respuestasActualizadas
                            )
                        }
                    } else {
                        // Si es comentario principal, recargar comentarios
                        val resultadoComentarios = comunidadRepository.obtenerComentarios(recetaId)
                        if (resultadoComentarios.isSuccess) {
                            _uiState.value = _uiState.value.copy(
                                comentarios = resultadoComentarios.getOrNull() ?: emptyList()
                            )
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al editar: ${resultado.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Toggle like
     */
    fun toggleLike(recetaId: String) {
        viewModelScope.launch {
            try {
                // Actualización optimista: cambiar UI inmediatamente
                val recetaActual = _uiState.value.receta
                if (recetaActual != null) {
                    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserId != null) {
                        val yaLeDioLike = recetaActual.usuariosQueLikean.contains(currentUserId)
                        val nuevosLikes = if (yaLeDioLike) {
                            recetaActual.likes - 1
                        } else {
                            recetaActual.likes + 1
                        }
                        val nuevosUsuarios = if (yaLeDioLike) {
                            recetaActual.usuariosQueLikean - currentUserId
                        } else {
                            recetaActual.usuariosQueLikean + currentUserId
                        }

                        // Actualizar UI inmediatamente
                        _uiState.value = _uiState.value.copy(
                            receta = recetaActual.copy(
                                likes = nuevosLikes,
                                usuariosQueLikean = nuevosUsuarios
                            )
                        )
                    }
                }

                // Hacer la operación en Firestore
                val resultado = comunidadRepository.toggleLike(recetaId)
                if (resultado.isSuccess) {
                    // Recargar receta después de un delay para confirmar
                    kotlinx.coroutines.delay(500)
                    cargarReceta(recetaId)
                } else {
                    // Si falla, revertir cambios
                    cargarReceta(recetaId)
                    _uiState.value = _uiState.value.copy(
                        error = "Error al dar like: ${resultado.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Seleccionar comentario para responder
     */
    @Suppress("UNUSED_PARAMETER")
    fun seleccionarComentarioParaResponder(comentarioId: String) {
        // Esta función se puede usar para scroll o focus
    }

    /**
     * Limpiar error
     */
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Activar scroll a comentarios
     */
    fun activarScrollAComentarios() {
        _uiState.value = _uiState.value.copy(scrollToComments = true)
    }

    /**
     * Desactivar scroll a comentarios (después de hacer el scroll)
     */
    fun desactivarScrollAComentarios() {
        _uiState.value = _uiState.value.copy(scrollToComments = false)
    }

    /**
     * Alternar favorito de receta de comunidad
     */
    fun alternarFavorito(recetaId: String) {
        viewModelScope.launch {
            try {
                _uiState.value.receta?.let { receta ->
                    val nuevoEstado = !receta.esFavorito
                    
                    // Actualizar en el repositorio
                    val resultado = comunidadRepository.actualizarFavoritoRecetaComunidad(recetaId, nuevoEstado)
                    
                    if (resultado.isSuccess) {
                        // Recargar la receta completa desde Firestore para asegurar sincronización
                        cargarReceta(recetaId)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Error al actualizar favorito"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}"
                )
            }
        }
    }
}

