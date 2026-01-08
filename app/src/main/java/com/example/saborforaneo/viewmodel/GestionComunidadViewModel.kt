package com.example.saborforaneo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.model.RecetaComunidad
import com.example.saborforaneo.notifications.NotificacionesManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

data class RecetasComunidadState(
    val recetas: List<RecetaComunidad> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

data class EstadisticasComunidad(
    val totalPublicadas: Int = 0,
    val totalPendientes: Int = 0,
    val totalRechazadas: Int = 0,
    val totalFavoritos: Int = 0,
    val autoresActivos: Int = 0,
    val recetasHoy: Int = 0
)

class GestionComunidadViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val recetasCollection = firestore.collection("recetasComunidad")
    private val notificacionesManager = NotificacionesManager(application)

    private val _recetas = MutableStateFlow(RecetasComunidadState())
    val recetas: StateFlow<RecetasComunidadState> = _recetas.asStateFlow()

    private val _estadisticas = MutableStateFlow(EstadisticasComunidad())
    val estadisticas: StateFlow<EstadisticasComunidad> = _estadisticas.asStateFlow()

    fun cargarRecetasComunidad() {
        viewModelScope.launch {
            _recetas.value = _recetas.value.copy(cargando = true, error = null)

            try {
                val snapshot = recetasCollection
                    .get()
                    .await()

                val listaRecetas = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        RecetaComunidad(
                            id = doc.id,
                            nombre = data["nombre"] as? String ?: "",
                            descripcion = data["descripcion"] as? String ?: "",
                            imagenUrl = data["imagenUrl"] as? String ?: "",
                            tiempoPreparacion = (data["tiempoPreparacion"] as? Long)?.toInt() ?: 0,
                            porciones = (data["porciones"] as? Long)?.toInt() ?: 1,
                            categoria = data["categoria"] as? String ?: "",
                            ingredientes = (data["ingredientes"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            pasos = (data["pasos"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            autorId = data["autorId"] as? String ?: data["autorUid"] as? String ?: "",
                            autorUid = data["autorUid"] as? String ?: data["autorId"] as? String ?: "",
                            nombreAutor = data["nombreAutor"] as? String ?: data["autorNombre"] as? String ?: "",
                            autorNombre = data["autorNombre"] as? String ?: data["nombreAutor"] as? String ?: "",
                            autorFoto = data["autorFoto"] as? String ?: "",
                            fechaCreacion = data["fechaCreacion"] as? Long ?: System.currentTimeMillis(),
                            publicada = data["publicada"] as? Boolean ?: false,
                            rechazada = data["rechazada"] as? Boolean ?: false,
                            fechaPublicacion = data["fechaPublicacion"] as? Long ?: 0,
                            totalFavoritos = (data["totalFavoritos"] as? Long)?.toInt() ?: 0,
                            activa = data["activa"] as? Boolean ?: true
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.fechaCreacion }

                _recetas.value = _recetas.value.copy(
                    recetas = listaRecetas,
                    cargando = false
                )

                // Calcular estadísticas
                calcularEstadisticas(listaRecetas)

            } catch (e: Exception) {
                _recetas.value = _recetas.value.copy(
                    error = "Error: ${e.message}",
                    cargando = false
                )
            }
        }
    }

    private fun calcularEstadisticas(recetas: List<RecetaComunidad>) {
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        _estadisticas.value = EstadisticasComunidad(
            totalPublicadas = recetas.count { it.publicada },
            totalPendientes = recetas.count { !it.publicada && !it.rechazada },
            totalRechazadas = recetas.count { it.rechazada },
            totalFavoritos = recetas.sumOf { it.totalFavoritos },
            autoresActivos = recetas.map { it.autorId }.distinct().size,
            recetasHoy = recetas.count { it.fechaCreacion >= hoy }
        )
    }

    fun publicarReceta(recetaId: String) {
        viewModelScope.launch {
            try {
                // Obtener datos de la receta antes de publicar
                val recetaSnapshot = recetasCollection.document(recetaId).get().await()
                val recetaData = recetaSnapshot.data
                val tituloReceta = recetaData?.get("nombre") as? String ?: "Nueva receta"
                val descripcion = recetaData?.get("descripcion") as? String ?: ""
                
                // Actualizar estado de publicación
                recetasCollection.document(recetaId)
                    .update(
                        mapOf(
                            "publicada" to true,
                            "rechazada" to false,
                            "fechaPublicacion" to System.currentTimeMillis()
                        )
                    )
                    .await()

                // Enviar notificación a todos los usuarios
                notificacionesManager.notificarNuevaRecetaAdmin(tituloReceta, descripcion)

                // Recargar recetas
                cargarRecetasComunidad()
            } catch (e: Exception) {
                _recetas.value = _recetas.value.copy(
                    error = "Error al publicar receta: ${e.message}"
                )
            }
        }
    }

    fun rechazarReceta(recetaId: String) {
        viewModelScope.launch {
            try {
                recetasCollection.document(recetaId)
                    .update(
                        mapOf(
                            "publicada" to false,
                            "rechazada" to true
                        )
                    )
                    .await()

                // Recargar recetas
                cargarRecetasComunidad()
            } catch (e: Exception) {
                _recetas.value = _recetas.value.copy(
                    error = "Error al rechazar receta: ${e.message}"
                )
            }
        }
    }

    fun eliminarReceta(recetaId: String) {
        viewModelScope.launch {
            try {
                recetasCollection.document(recetaId)
                    .delete()
                    .await()

                // Recargar recetas
                cargarRecetasComunidad()
            } catch (e: Exception) {
                _recetas.value = _recetas.value.copy(
                    error = "Error al eliminar receta: ${e.message}"
                )
            }
        }
    }

    fun restaurarReceta(recetaId: String) {
        viewModelScope.launch {
            try {
                recetasCollection.document(recetaId)
                    .update(
                        mapOf(
                            "publicada" to false,
                            "rechazada" to false
                        )
                    )
                    .await()

                // Recargar recetas
                cargarRecetasComunidad()
            } catch (e: Exception) {
                _recetas.value = _recetas.value.copy(
                    error = "Error al restaurar receta: ${e.message}"
                )
            }
        }
    }
}
