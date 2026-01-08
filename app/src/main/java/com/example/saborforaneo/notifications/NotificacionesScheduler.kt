package com.example.saborforaneo.notifications

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Configurador de notificaciones periódicas usando WorkManager
 */
object NotificacionesScheduler {

    private const val RECORDATORIO_WORK_NAME = "recordatorio_diario"

    /**
     * Programa notificaciones periódicas de recordatorio
     * @param context Contexto de la aplicación
     * @param intervaloHoras Intervalo en horas entre notificaciones (por defecto 24 horas)
     */
    fun programarRecordatorios(context: Context, intervaloHoras: Long = 24) {
        try {
            val recordatorioRequest = PeriodicWorkRequestBuilder<RecordatorioWorker>(
                intervaloHoras, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                RECORDATORIO_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Mantiene el trabajo existente si ya está programado
                recordatorioRequest
            )

            Log.d("NotificacionesScheduler", "Recordatorios programados cada $intervaloHoras horas")
        } catch (e: Exception) {
            Log.e("NotificacionesScheduler", "Error al programar recordatorios", e)
        }
    }

    /**
     * Cancela todos los recordatorios programados
     */
    fun cancelarRecordatorios(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(RECORDATORIO_WORK_NAME)
            Log.d("NotificacionesScheduler", "Recordatorios cancelados")
        } catch (e: Exception) {
            Log.e("NotificacionesScheduler", "Error al cancelar recordatorios", e)
        }
    }

    /**
     * Verifica si los recordatorios están activos
     */
    fun verificarEstado(context: Context) {
        try {
            val workInfo = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(RECORDATORIO_WORK_NAME)
            
            workInfo.get().forEach { info ->
                Log.d("NotificacionesScheduler", "Estado del trabajo: ${info.state}")
            }
        } catch (e: Exception) {
            Log.e("NotificacionesScheduler", "Error al verificar estado", e)
        }
    }
}
