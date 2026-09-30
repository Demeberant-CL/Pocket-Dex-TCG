package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import java.util.concurrent.TimeUnit

/**
 * Worker en segundo plano para notificar al jugador digital cuando se recarga su sobre gratuito.
 * En Pokémon TCG Pocket, la energía de sobres se recarga cada 12 horas (máximo 2 sobres acumulables).
 */
class PackRechargeWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "pocket_pack_recharge_channel"
        const val CHANNEL_NAME = "Recarga de Sobres TCG Pocket"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME_ONE_TIME = "pack_recharge_12h_work"
        const val WORK_NAME_PERIODIC = "pack_recharge_periodic_work"
    }

    override suspend fun doWork(): Result {
        sendPackRechargeNotification()
        return Result.success()
    }

    private fun sendPackRechargeNotification() {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android 8.0 (API 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones automáticas de recarga de sobres gratuitos en Pokémon TCG Pocket"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la aplicación al tocar la notificación
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Tu sobre gratuito está listo! 🎴")
            .setContentText("Tu energía de sobres de Pokémon TCG Pocket se ha recargado. ¡Abre tu nuevo sobre ahora!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("¡Tu medidor de energía ha alcanzado el 100%! Tienes 1 sobre gratuito disponible para abrir en Pokémon TCG Pocket. Entra a descubrir tus nuevas cartas.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

/**
 * Gestor de programación para WorkManager.
 */
object PackNotificationScheduler {

    /**
     * Programa una notificación exacta de recarga tras 12 horas (ciclo oficial de Pokémon TCG Pocket).
     */
    fun schedule12HourRecharge(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PackRechargeWorker>()
            .setInitialDelay(12, TimeUnit.HOURS)
            .addTag("recharge_12h")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            PackRechargeWorker.WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Programa una notificación periódica cada 12 horas.
     */
    fun schedulePeriodicRecharge(context: Context) {
        val periodicRequest = PeriodicWorkRequestBuilder<PackRechargeWorker>(12, TimeUnit.HOURS)
            .addTag("recharge_periodic")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PackRechargeWorker.WORK_NAME_PERIODIC,
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    /**
     * Para pruebas inmediatas: programa una notificación en 5 segundos.
     */
    fun scheduleQuickTestNotification(context: Context, delaySeconds: Long = 5) {
        val testRequest = OneTimeWorkRequestBuilder<PackRechargeWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .addTag("recharge_test")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "pack_recharge_test_work",
            ExistingWorkPolicy.REPLACE,
            testRequest
        )
    }

    /**
     * Cancela todas las notificaciones programadas.
     */
    fun cancelAllRechargeNotifications(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(PackRechargeWorker.WORK_NAME_ONE_TIME)
        workManager.cancelUniqueWork(PackRechargeWorker.WORK_NAME_PERIODIC)
        workManager.cancelUniqueWork("pack_recharge_test_work")
    }
}
