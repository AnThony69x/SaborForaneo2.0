package com.example.saborforaneo.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saborforaneo.data.model.EstadoModeracion
import com.example.saborforaneo.data.model.Receta
import com.example.saborforaneo.data.model.Dificultad
import com.example.saborforaneo.data.model.Precio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ModeracionViewModel(private val context: Context) : ViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _recetasPendientes = MutableStateFlow<List<Receta>>(emptyList())
    val recetasPendientes: StateFlow<List<Receta>> = _recetasPendientes.asStateFlow()
    
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()
    
    fun cargarRecetasPendientes() {
        viewModelScope.launch {
            try {
                _cargando.value = true
                
                val snapshot = firestore.collection("recetas")
                    .whereEqualTo("estadoModeracion", EstadoModeracion.PENDIENTE.name)
                    .get()
                    .await()
                
                val recetas = snapshot.documents.mapNotNull { doc ->
                    try {
                        Receta(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            imagenUrl = doc.getString("imagenUrl") ?: "",
                            creadoPor = doc.getString("creadoPor") ?: "",
                            categoria = doc.getString("categoria") ?: "",
                            tiempoPreparacion = doc.getLong("tiempoPreparacion")?.toInt() ?: 0,
                            dificultad = try {
                                Dificultad.valueOf(doc.getString("dificultad") ?: "MEDIA")
                            } catch (e: Exception) {
                                Dificultad.MEDIA
                            },
                            precio = try {
                                Precio.valueOf(doc.getString("precio") ?: "MODERADO")
                            } catch (e: Exception) {
                                Precio.MODERADO
                            },
                            ingredientes = (doc.get("ingredientes") as? List<*>)
                                ?.mapNotNull { it as? String } ?: emptyList(),
                            pasos = (doc.get("pasos") as? List<*>)
                                ?.mapNotNull { it as? String } ?: emptyList(),
                            fechaCreacion = doc.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                            activa = doc.getBoolean("activa") ?: true,
                            estadoModeracion = EstadoModeracion.valueOf(
                                doc.getString("estadoModeracion") ?: EstadoModeracion.PENDIENTE.name
                            ),
                            moderadoPor = doc.getString("moderadoPor") ?: "",
                            fechaModeracion = doc.getLong("fechaModeracion") ?: 0L,
                            motivoRechazo = doc.getString("motivoRechazo") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("ModeracionVM", "Error al mapear receta: ${e.message}")
                        null
                    }
                }
                
                // Ordenar por fecha de creación (más recientes primero)
                _recetasPendientes.value = recetas.sortedByDescending { it.fechaCreacion }
                
                Log.d("ModeracionVM", "Recetas pendientes cargadas: ${recetas.size}")
                
            } catch (e: Exception) {
                Log.e("ModeracionVM", "Error al cargar recetas pendientes: ${e.message}")
                _recetasPendientes.value = emptyList()
            } finally {
                _cargando.value = false
            }
        }
    }
    
    suspend fun aprobarReceta(recetaId: String, adminUid: String): Boolean {
        return try {
            firestore.collection("recetas")
                .document(recetaId)
                .update(
                    mapOf(
                        "estadoModeracion" to EstadoModeracion.APROBADA.name,
                        "moderadoPor" to adminUid,
                        "fechaModeracion" to System.currentTimeMillis(),
                        "motivoRechazo" to ""
                    )
                )
                .await()
            
            // Actualizar lista local
            _recetasPendientes.value = _recetasPendientes.value.filter { it.id != recetaId }
            
            Log.d("ModeracionVM", "Receta $recetaId aprobada por $adminUid")
            true
            
        } catch (e: Exception) {
            Log.e("ModeracionVM", "Error al aprobar receta: ${e.message}")
            false
        }
    }
    
    suspend fun rechazarReceta(recetaId: String, adminUid: String, motivo: String): Boolean {
        return try {
            firestore.collection("recetas")
                .document(recetaId)
                .update(
                    mapOf(
                        "estadoModeracion" to EstadoModeracion.RECHAZADA.name,
                        "moderadoPor" to adminUid,
                        "fechaModeracion" to System.currentTimeMillis(),
                        "motivoRechazo" to motivo,
                        "activa" to false  // Desactivar receta rechazada
                    )
                )
                .await()
            
            // Actualizar lista local
            _recetasPendientes.value = _recetasPendientes.value.filter { it.id != recetaId }
            
            Log.d("ModeracionVM", "Receta $recetaId rechazada por $adminUid. Motivo: $motivo")
            true
            
        } catch (e: Exception) {
            Log.e("ModeracionVM", "Error al rechazar receta: ${e.message}")
            false
        }
    }
}
