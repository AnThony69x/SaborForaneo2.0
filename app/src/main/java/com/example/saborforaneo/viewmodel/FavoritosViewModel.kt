package com.example.saborforaneo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.model.Receta
import com.example.saborforaneo.data.repository.FirestoreRepository
import com.example.saborforaneo.data.repository.RecetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritosUiState(
    val recetasFavoritas: List<Receta> = emptyList(),
    val idsFavoritos: Set<String> = emptySet(),
    val cargando: Boolean = true,
    val error: String? = null
)

class FavoritosViewModel(context: Context) : ViewModel() {
    private val recetaRepository = RecetaRepository(context)
    private val firestoreRepository = FirestoreRepository()

    private val _uiState = MutableStateFlow(FavoritosUiState())
    val uiState: StateFlow<FavoritosUiState> = _uiState.asStateFlow()

    init {
        cargarFavoritos()
    }

    fun cargarFavoritos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            try {
                // Obtener IDs de favoritos del usuario
                val resultadoIds = firestoreRepository.obtenerFavoritos()
                
                if (resultadoIds.isSuccess) {
                    val idsFavoritos = resultadoIds.getOrNull() ?: emptyList()
                    
                    // Obtener detalles de las recetas favoritas
                    val recetasFavoritas = mutableListOf<Receta>()
                    
                    for (id in idsFavoritos) {
                        val resultadoReceta = recetaRepository.obtenerRecetaPorId(id)
                        resultadoReceta.getOrNull()?.let { receta ->
                            recetasFavoritas.add(receta.copy(esFavorito = true))
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        recetasFavoritas = recetasFavoritas,
                        idsFavoritos = idsFavoritos.toSet(),
                        cargando = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al cargar favoritos: ${resultadoIds.exceptionOrNull()?.message}",
                        cargando = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}",
                    cargando = false
                )
            }
        }
    }

    fun toggleFavorito(recetaId: String) {
        viewModelScope.launch {
            try {
                val esFavorito = _uiState.value.idsFavoritos.contains(recetaId)
                
                val resultado = if (esFavorito) {
                    firestoreRepository.quitarFavorito(recetaId)
                } else {
                    firestoreRepository.agregarFavorito(recetaId)
                }
                
                if (resultado.isSuccess) {
                    // Recargar favoritos
                    cargarFavoritos()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar favorito: ${e.message}"
                )
            }
        }
    }

    fun esFavorito(recetaId: String): Boolean {
        return _uiState.value.idsFavoritos.contains(recetaId)
    }
}

