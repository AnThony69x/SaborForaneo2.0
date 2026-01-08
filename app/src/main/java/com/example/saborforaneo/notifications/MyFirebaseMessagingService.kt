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
import com.example.saborforaneo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token actualizado: $token")
        
        // Guardar el token en Firestore para el usuario actual
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            guardarTokenEnFirestore(userId, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d("FCM", "Mensaje recibido de: ${message.from}")
        
        // Verificar si el mensaje tiene datos
        message.data.isNotEmpty().let {
            Log.d("FCM", "Datos del mensaje: ${message.data}")
            
            val titulo = message.data["titulo"] ?: message.notification?.title ?: "Nueva notificación"
            val cuerpo = message.data["cuerpo"] ?: message.notification?.body ?: ""
            val tipo = message.data["tipo"] ?: "general"
            
            mostrarNotificacion(titulo, cuerpo, tipo)
        }
        
        // Verificar si el mensaje tiene notificación
        message.notification?.let {
            Log.d("FCM", "Notificación recibida: ${it.title}")
            mostrarNotificacion(
                it.title ?: "Nueva notificación",
                it.body ?: "",
                "general"
            )
        }
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String, tipo: String) {
        val channelId = when (tipo) {
            "nueva_receta_admin" -> CHANNEL_ADMIN_RECETA
            "nueva_receta_comunidad" -> CHANNEL_COMUNIDAD_RECETA
            "recordatorio" -> CHANNEL_RECORDATORIO
            else -> CHANNEL_GENERAL
        }
        
        crearCanalesNotificacion()
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("tipo_notificacion", tipo)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            tipo.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val icono = when (tipo) {
            "nueva_receta_admin" -> "🎉"
            "nueva_receta_comunidad" -> "👥"
            "recordatorio" -> "🔔"
            else -> "📱"
        }
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$icono $titulo")
            .setContentText(cuerpo)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(cuerpo)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun crearCanalesNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para recetas publicadas por admin
            val channelAdmin = NotificationChannel(
                CHANNEL_ADMIN_RECETA,
                "Recetas del Administrador",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando el administrador publica una nueva receta"
                enableVibration(true)
                enableLights(true)
            }
            
            // Canal para recetas de la comunidad
            val channelComunidad = NotificationChannel(
                CHANNEL_COMUNIDAD_RECETA,
                "Recetas de la Comunidad",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando usuarios publican recetas en la comunidad"
                enableVibration(true)
            }
            
            // Canal para recordatorios
            val channelRecordatorio = NotificationChannel(
                CHANNEL_RECORDATORIO,
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios para usar la aplicación"
            }
            
            // Canal general
            val channelGeneral = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de la aplicación"
            }
            
            notificationManager.createNotificationChannel(channelAdmin)
            notificationManager.createNotificationChannel(channelComunidad)
            notificationManager.createNotificationChannel(channelRecordatorio)
            notificationManager.createNotificationChannel(channelGeneral)
        }
    }

    private fun guardarTokenEnFirestore(userId: String, token: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FCM", "Token guardado en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error al guardar token", e)
            }
    }

    companion object {
        const val CHANNEL_ADMIN_RECETA = "admin_receta_channel"
        const val CHANNEL_COMUNIDAD_RECETA = "comunidad_receta_channel"
        const val CHANNEL_RECORDATORIO = "recordatorio_channel"
        const val CHANNEL_GENERAL = "general_channel"
    }
}
