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

data class HomeUiState(
    val recetas: List<Receta> = emptyList(),
    val idsFavoritos: Set<String> = emptySet(),
    val cargando: Boolean = true,
    val error: String? = null
)

class HomeViewModel(context: Context) : ViewModel() {
    private val repository = RecetaRepository(context)
    private val firestoreRepository = FirestoreRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarRecetas()
        cargarFavoritos()
    }

    fun cargarRecetas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val resultado = repository.obtenerTodasLasRecetas()

            if (resultado.isSuccess) {
                val recetas = resultado.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    recetas = marcarFavoritos(recetas),
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

    private fun cargarFavoritos() {
        viewModelScope.launch {
            val resultado = firestoreRepository.obtenerFavoritos()
            if (resultado.isSuccess) {
                val favoritos = resultado.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    idsFavoritos = favoritos.toSet(),
                    recetas = marcarFavoritos(_uiState.value.recetas)
                )
            }
        }
    }
    
    // Función pública para recargar favoritos desde las pantallas
    fun recargarFavoritos() {
        cargarFavoritos()
    }

    private fun marcarFavoritos(recetas: List<Receta>): List<Receta> {
        val favoritos = _uiState.value.idsFavoritos
        return recetas.map { receta ->
            receta.copy(esFavorito = favoritos.contains(receta.id))
        }
    }

    fun toggleFavorito(recetaId: String) {
        viewModelScope.launch {
            val esFavorito = _uiState.value.idsFavoritos.contains(recetaId)
            
            val resultado = if (esFavorito) {
                firestoreRepository.quitarFavorito(recetaId)
            } else {
                firestoreRepository.agregarFavorito(recetaId)
            }
            
            if (resultado.isSuccess) {
                cargarFavoritos()
            }
        }
    }

    fun obtenerRecetasPorCategoria(categoria: String?): List<Receta> {
        val recetas = _uiState.value.recetas

        return if (categoria == null) {
            recetas
        } else {
            when (categoria) {
                "Rápidas" -> recetas.filter { it.tiempoPreparacion <= 30 }
                "Vegetariana" -> recetas.filter { it.esVegetariana }
                "Económica" -> recetas.filter { it.precio.name == "ECONOMICO" }
                else -> recetas.filter { receta ->
                    receta.categoria.contains(categoria, ignoreCase = true)
                }
            }
        }
    }
}

