package com.example.saborforaneo.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker para enviar notificaciones periódicas de recordatorio
 */
class RecordatorioWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val notificacionesManager = NotificacionesManager(applicationContext)
            notificacionesManager.mostrarRecordatorioApp()
            
            Log.d("RecordatorioWorker", "Notificación de recordatorio enviada")
            Result.success()
        } catch (e: Exception) {
            Log.e("RecordatorioWorker", "Error al enviar recordatorio", e)
            Result.failure()
        }
    }
}
