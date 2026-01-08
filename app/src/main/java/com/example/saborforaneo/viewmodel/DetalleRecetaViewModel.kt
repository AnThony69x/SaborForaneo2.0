package com.example.saborforaneo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.model.Receta
import com.example.saborforaneo.data.repository.RecetaRepository
import com.example.saborforaneo.data.repository.FirestoreRepository
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
    private val recetaRepository = RecetaRepository(context)
    private val firestoreRepository = FirestoreRepository()

    private val _uiState = MutableStateFlow(DetalleRecetaUiState())
    val uiState: StateFlow<DetalleRecetaUiState> = _uiState.asStateFlow()

    init {
        cargarReceta()
    }

    private fun cargarReceta() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val resultadoReceta = recetaRepository.obtenerRecetaPorId(recetaId)
            
            if (resultadoReceta.isSuccess) {
                val receta = resultadoReceta.getOrNull()
                
                if (receta != null) {
                    // Verificar si está en favoritos del usuario
                    val resultadoFavorito = firestoreRepository.esFavorito(recetaId)
                    val esFavorito = resultadoFavorito.getOrNull() ?: false
                    
                    _uiState.value = _uiState.value.copy(
                        receta = receta.copy(esFavorito = esFavorito),
                        cargando = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        receta = null,
                        cargando = false,
                        error = "Receta no encontrada"
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar receta: ${resultadoReceta.exceptionOrNull()?.message}",
                    cargando = false
                )
            }
        }
    }

    fun alternarFavorito() {
        viewModelScope.launch {
            _uiState.value.receta?.let { receta ->
                val esFavorito = receta.esFavorito
                
                val resultado = if (esFavorito) {
                    firestoreRepository.quitarFavorito(receta.id)
                } else {
                    firestoreRepository.agregarFavorito(receta.id)
                }
                
                if (resultado.isSuccess) {
                    // Recargar la receta para obtener el estado actualizado
                    cargarReceta()
                }
            }
        }
    }
}

