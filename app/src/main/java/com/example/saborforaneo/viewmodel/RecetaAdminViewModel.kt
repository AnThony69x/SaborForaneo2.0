package com.example.saborforaneo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.model.Receta
import com.example.saborforaneo.data.repository.RecetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecetaAdminUiState(
    val recetas: List<Receta> = emptyList(),
    val recetaSeleccionada: Receta? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val operacionExitosa: Boolean = false,
    val mensajeExito: String? = null
)

class RecetaAdminViewModel(context: Context) : ViewModel() {
    private val repository = RecetaRepository(context)

    private val _uiState = MutableStateFlow(RecetaAdminUiState())
    val uiState: StateFlow<RecetaAdminUiState> = _uiState.asStateFlow()

    init {
        cargarRecetasAdmin()
    }

    /**
     * Cargar solo las recetas creadas por admin (no las locales)
     */
    fun cargarRecetasAdmin() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val resultado = repository.obtenerRecetasAdmin()

            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    recetas = resultado.getOrNull() ?: emptyList(),
                    cargando = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar recetas: ${resultado.exceptionOrNull()?.message}",
                    cargando = false
                )
            }
        }
    }

    /**
     * Cargar todas las recetas (locales + admin)
     */
    fun cargarTodasLasRecetas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val resultado = repository.obtenerTodasLasRecetas()

            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    recetas = resultado.getOrNull() ?: emptyList(),
                    cargando = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar recetas: ${resultado.exceptionOrNull()?.message}",
                    cargando = false
                )
            }
        }
    }

    /**
     * Agregar nueva receta
     */
    fun agregarReceta(receta: Receta, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val resultado = repository.agregarReceta(receta, userId)

            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    operacionExitosa = true,
                    mensajeExito = "Receta agregada exitosamente",
                    cargando = false
                )
                cargarRecetasAdmin()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Error al agregar receta: ${resultado.exceptionOrNull()?.message}",
                    cargando = false
                )
            }
        }
    }

    /**
     * Actualizar receta existente
     */
    fun actualizarReceta(id: String, receta: Receta) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val resultado = repository.actualizarReceta(id, receta)

            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    operacionExitosa = true,
                    mensajeExito = "Receta actualizada exitosamente",
                    cargando = false
                )
                cargarRecetasAdmin()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = resultado.exceptionOrNull()?.message ?: "Error desconocido",
                    cargando = false
                )
            }
        }
    }

    /**
     * Eliminar receta (lógicamente)
     */
    fun eliminarReceta(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val resultado = repository.eliminarReceta(id)

            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    operacionExitosa = true,
                    mensajeExito = "Receta eliminada exitosamente",
                    cargando = false
                )
                cargarRecetasAdmin()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = resultado.exceptionOrNull()?.message ?: "Error desconocido",
                    cargando = false
                )
            }
        }
    }

    /**
     * Seleccionar receta para editar
     */
    fun seleccionarReceta(receta: Receta?) {
        _uiState.value = _uiState.value.copy(recetaSeleccionada = receta)
    }

    /**
     * Buscar recetas
     */
    fun buscarRecetas(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val resultado = repository.buscarRecetas(query)

            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    recetas = resultado.getOrNull() ?: emptyList(),
                    cargando = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Error al buscar recetas: ${resultado.exceptionOrNull()?.message}",
                    cargando = false
                )
            }
        }
    }

    /**
     * Limpiar estado de operación
     */
    fun limpiarEstadoOperacion() {
        _uiState.value = _uiState.value.copy(
            operacionExitosa = false,
            error = null,
            mensajeExito = null
        )
    }

    /**
     * Limpiar error
     */
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

