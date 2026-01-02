package com.example.saborforaneo.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.saborforaneo.MainActivity

object RecetaDiariaNotification {

    private const val CHANNEL_ID = "receta_diaria_channel"
    private const val CHANNEL_NAME = "Recetas Diarias"
    private const val NOTIFICATION_ID = 1001

    fun crearCanalNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de recetas recomendadas"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun mostrarNotificacionPrueba(context: Context) {
        crearCanalNotificacion(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔔 ¡Notificaciones Activadas!")
            .setContentText("Recibirás recetas deliciosas todos los días")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🍽️ ¡Bienvenido a SaborForaneo!\n\nRecibirás notificaciones con las mejores recetas ecuatorianas e internacionales.\n\n👉 Toca para explorar recetas")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
        }
    }

    fun mostrarNotificacionReceta(
        context: Context,
        tituloReceta: String,
        descripcion: String
    ) {
        crearCanalNotificacion(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🍽️ Receta del Día")
            .setContentText(tituloReceta)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$tituloReceta\n\n$descripcion\n\n👉 Toca para ver la receta completa")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID + 1, notification)
        } catch (e: SecurityException) {
        }
    }
}