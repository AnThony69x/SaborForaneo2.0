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

data class DetalleRecetaUiState(
    val receta: Receta? = null,
    val cargando: Boolean = true,
    val error: String? = null
)

class DetalleRecetaViewModel(
    private val context: Context,
    private val recetaId: String
) : ViewModel() {
    private val repository = RecetaRepository(context)

    private val _uiState = MutableStateFlow(DetalleRecetaUiState())
    val uiState: StateFlow<DetalleRecetaUiState> = _uiState.asStateFlow()

    init {
        cargarReceta()
    }

    private fun cargarReceta() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val resultado = repository.obtenerRecetaPorId(recetaId)

            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    receta = resultado.getOrNull(),
                    cargando = false,
                    error = if (resultado.getOrNull() == null) "Receta no encontrada" else null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar receta: ${resultado.exceptionOrNull()?.message}",
                    cargando = false
                )
            }
        }
    }
}

