package com.example.saborforaneo.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.saborforaneo.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NotificacionesManager(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        crearCanalesNotificacion()
    }

    /**
     * Crea los canales de notificación necesarios
     */
    private fun crearCanalesNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ADMIN_RECETA,
                    "Recetas del Administrador",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones cuando el administrador publica una nueva receta"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_COMUNIDAD_RECETA,
                    "Recetas de la Comunidad",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones cuando usuarios publican recetas en la comunidad"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_RECORDATORIO,
                    "Recordatorios",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Recordatorios para usar la aplicación"
                },
                NotificationChannel(
                    CHANNEL_GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones generales de la aplicación"
                }
            )
            
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    /**
     * Notifica a todos los usuarios cuando el admin publica una receta
     */
    suspend fun notificarNuevaRecetaAdmin(tituloReceta: String, descripcion: String) {
        withContext(Dispatchers.IO) {
            try {
                // Obtener todos los tokens FCM de usuarios
                val usuariosSnapshot = db.collection("usuarios")
                    .whereNotEqualTo("fcmToken", null)
                    .get()
                    .await()
                
                val tokens = usuariosSnapshot.documents.mapNotNull { it.getString("fcmToken") }
                
                if (tokens.isNotEmpty()) {
                    // Aquí normalmente enviarías las notificaciones a través de tu backend
                    // Por ahora, mostraremos una notificación local como ejemplo
                    mostrarNotificacionLocal(
                        titulo = "🎉 Nueva Receta Publicada",
                        cuerpo = "$tituloReceta\n\n${descripcion.take(100)}...",
                        channelId = CHANNEL_ADMIN_RECETA,
                        tipo = "nueva_receta_admin"
                    )
                    
                    Log.d("NotificacionesManager", "Notificación enviada a ${tokens.size} usuarios")
                }
            } catch (e: Exception) {
                Log.e("NotificacionesManager", "Error al enviar notificación de receta admin", e)
            }
        }
    }

    /**
     * Notifica cuando un usuario crea una nueva receta en la comunidad
     */
    suspend fun notificarNuevaRecetaComunidad(tituloReceta: String, nombreAutor: String) {
        withContext(Dispatchers.IO) {
            try {
                // Obtener tokens de usuarios interesados (podrías filtrar por preferencias)
                val usuariosSnapshot = db.collection("usuarios")
                    .whereNotEqualTo("fcmToken", null)
                    .get()
                    .await()
                
                val tokens = usuariosSnapshot.documents.mapNotNull { it.getString("fcmToken") }
                
                if (tokens.isNotEmpty()) {
                    mostrarNotificacionLocal(
                        titulo = "👥 Nueva Receta de la Comunidad",
                        cuerpo = "$nombreAutor compartió: $tituloReceta",
                        channelId = CHANNEL_COMUNIDAD_RECETA,
                        tipo = "nueva_receta_comunidad"
                    )
                    
                    Log.d("NotificacionesManager", "Notificación de comunidad enviada a ${tokens.size} usuarios")
                }
            } catch (e: Exception) {
                Log.e("NotificacionesManager", "Error al enviar notificación de comunidad", e)
            }
        }
    }

    /**
     * Muestra un recordatorio para usar la app
     */
    fun mostrarRecordatorioApp() {
        val mensajes = listOf(
            "¿Qué tal una nueva receta hoy? 🍳",
            "Descubre sabores únicos en SaborForaneo 🌎",
            "¡Hora de cocinar algo delicioso! 👨‍🍳",
            "Tenemos recetas increíbles esperándote 🍽️",
            "¿Ya probaste las recetas de la comunidad? 👥"
        )
        
        mostrarNotificacionLocal(
            titulo = "🔔 ¡Te extrañamos!",
            cuerpo = mensajes.random(),
            channelId = CHANNEL_RECORDATORIO,
            tipo = "recordatorio"
        )
    }

    /**
     * Muestra una notificación local
     */
    private fun mostrarNotificacionLocal(
        titulo: String,
        cuerpo: String,
        channelId: String,
        tipo: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("tipo_notificacion", tipo)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            tipo.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(cuerpo)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            Log.e("NotificacionesManager", "Permiso de notificación denegado", e)
        }
    }

    /**
     * Registra el token FCM del usuario actual
     */
    suspend fun registrarTokenFCM(token: String) {
        withContext(Dispatchers.IO) {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext
                
                db.collection("usuarios").document(userId)
                    .update("fcmToken", token)
                    .await()
                
                Log.d("NotificacionesManager", "Token FCM registrado correctamente")
            } catch (e: Exception) {
                Log.e("NotificacionesManager", "Error al registrar token FCM", e)
            }
        }
    }

    companion object {
        const val CHANNEL_ADMIN_RECETA = "admin_receta_channel"
        const val CHANNEL_COMUNIDAD_RECETA = "comunidad_receta_channel"
        const val CHANNEL_RECORDATORIO = "recordatorio_channel"
        const val CHANNEL_GENERAL = "general_channel"
    }
}
