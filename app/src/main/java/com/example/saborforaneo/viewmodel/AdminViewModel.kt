package com.example.saborforaneo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.model.Receta
import com.example.saborforaneo.data.repository.FirestoreRepository
import com.example.saborforaneo.data.repository.RecetaRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EstadisticasAdmin(
    val totalRecetas: Int = 0,
    val totalUsuarios: Int = 0,
    val totalAdmins: Int = 0,
    val recetasHoy: Int = 0,
    val usuariosHoy: Int = 0,
    val recetasMasPopulares: List<RecetaPopular> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

data class RecetaPopular(
    val id: String = "",
    val nombre: String = "",
    val totalFavoritos: Int = 0
)

data class UsuarioInfo(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "usuario",
    val fechaCreacion: Long = 0L,
    val totalFavoritos: Int = 0
)

data class UsuariosUiState(
    val usuarios: List<UsuarioInfo> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class AdminViewModel(context: Context) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val recetaRepository = RecetaRepository(context)
    private val firestoreRepository = FirestoreRepository()

    private val _estadisticas = MutableStateFlow(EstadisticasAdmin())
    val estadisticas: StateFlow<EstadisticasAdmin> = _estadisticas.asStateFlow()

    private val _usuarios = MutableStateFlow(UsuariosUiState())
    val usuarios: StateFlow<UsuariosUiState> = _usuarios.asStateFlow()

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            _estadisticas.value = _estadisticas.value.copy(cargando = true, error = null)

            try {
                // Obtener total de recetas
                val resultadoRecetas = recetaRepository.obtenerTodasLasRecetas()
                val recetas = resultadoRecetas.getOrNull() ?: emptyList()
                val totalRecetas = recetas.size

                // Obtener recetas creadas hoy
                val hoyInicio = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                val recetasHoy = recetas.count { it.fechaCreacion >= hoyInicio }

                // Obtener todos los usuarios
                val snapshotUsuarios = db.collection("usuarios").get().await()
                val totalUsuarios = snapshotUsuarios.size()
                
                // Contar admins
                val totalAdmins = snapshotUsuarios.documents.count { 
                    it.getString("rol") == "admin" 
                }

                // Usuarios registrados hoy
                val usuariosHoy = snapshotUsuarios.documents.count {
                    val fechaCreacion = it.getLong("fechaCreacion") ?: 0L
                    fechaCreacion >= hoyInicio
                }

                // Obtener recetas más populares (por número de favoritos)
                val recetasConFavoritos = mutableListOf<RecetaPopular>()
                
                for (receta in recetas.take(50)) { // Limitar a 50 para no sobrecargar
                    var totalFavoritos = 0
                    
                    // Contar cuántos usuarios tienen esta receta en favoritos
                    snapshotUsuarios.documents.forEach { doc ->
                        val favoritos = doc.get("recetasFavoritas") as? List<*> ?: emptyList<String>()
                        if (favoritos.contains(receta.id)) {
                            totalFavoritos++
                        }
                    }
                    
                    recetasConFavoritos.add(
                        RecetaPopular(
                            id = receta.id,
                            nombre = receta.nombre,
                            totalFavoritos = totalFavoritos
                        )
                    )
                }

                // Ordenar por favoritos y tomar las top 5
                val top5 = recetasConFavoritos
                    .sortedByDescending { it.totalFavoritos }
                    .take(5)

                _estadisticas.value = EstadisticasAdmin(
                    totalRecetas = totalRecetas,
                    totalUsuarios = totalUsuarios,
                    totalAdmins = totalAdmins,
                    recetasHoy = recetasHoy,
                    usuariosHoy = usuariosHoy,
                    recetasMasPopulares = top5,
                    cargando = false
                )

            } catch (e: Exception) {
                _estadisticas.value = _estadisticas.value.copy(
                    cargando = false,
                    error = "Error al cargar estadísticas: ${e.message}"
                )
            }
        }
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _usuarios.value = _usuarios.value.copy(cargando = true, error = null)

            try {
                val snapshot = db.collection("usuarios")
                    .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val listaUsuarios = snapshot.documents.mapNotNull { doc ->
                    try {
                        val favoritos = doc.get("recetasFavoritas") as? List<*> ?: emptyList<String>()
                        UsuarioInfo(
                            uid = doc.id,
                            nombre = doc.getString("nombre") ?: "Sin nombre",
                            email = doc.getString("email") ?: "Sin email",
                            rol = doc.getString("rol") ?: "usuario",
                            fechaCreacion = doc.getLong("fechaCreacion") ?: 0L,
                            totalFavoritos = favoritos.size
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                _usuarios.value = _usuarios.value.copy(
                    usuarios = listaUsuarios,
                    cargando = false
                )

            } catch (e: Exception) {
                _usuarios.value = _usuarios.value.copy(
                    cargando = false,
                    error = "Error al cargar usuarios: ${e.message}"
                )
            }
        }
    }

    fun cambiarRolUsuario(uid: String, nuevoRol: String) {
        viewModelScope.launch {
            try {
                db.collection("usuarios")
                    .document(uid)
                    .update("rol", nuevoRol)
                    .await()

                // Recargar usuarios
                cargarUsuarios()
            } catch (e: Exception) {
                _usuarios.value = _usuarios.value.copy(
                    error = "Error al cambiar rol: ${e.message}"
                )
            }
        }
    }
}
