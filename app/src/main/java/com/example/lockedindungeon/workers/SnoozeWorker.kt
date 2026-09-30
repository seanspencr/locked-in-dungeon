package com.example.lockedindungeon.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.lockedindungeon.R
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

@HiltWorker
class SnoozeWorker @AssistedInject constructor(@Assisted val context : Context, @Assisted workerParams : WorkerParameters, private val datastoreRepository: AppDatastoreRepository)
: CoroutineWorker(appContext = context, params = workerParams) {



    private val tag = "SnoozeWorker"

    companion object {
        const val SNOOZE_NOTIF_ID : Int = 0
        const val SNOOZE_NOTIF_CHANNEL_ID : String = "snooze_notif_channel_v2"
        const val SNOOZE_NOTIF_CHANNEL_NAME : String = "Snooze Notification"
    }

    override suspend fun doWork(): Result {

        activateBlocking()
        publishNotif()
        return Result.success()
    }

    private suspend fun activateBlocking() {
        Log.d(tag, "Activating blocking")
        if (datastoreRepository.isActive.first()) return
        datastoreRepository.setActive(true)
    }

    private fun buildNotif() : Notification {
        return NotificationCompat.Builder(context, SNOOZE_NOTIF_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle("Reactivating Blocking")
            setContentText("Snooze duration ran out, reactivating blocking")
            setAutoCancel(true)
            setPriority(NotificationCompat.PRIORITY_HIGH) // <-- Kunci Utama 2 (untuk API < 26)
            setDefaults(NotificationCompat.DEFAULT_ALL) // Aktifkan suara & getar
        }.build()
    }

    private fun createNotificationChannel(){
        context.getSystemService<NotificationManager>()?.createNotificationChannel(
            NotificationChannel(SNOOZE_NOTIF_CHANNEL_ID, SNOOZE_NOTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private fun publishNotif(){
        createNotificationChannel()
        val notif = buildNotif()
        context.getSystemService<NotificationManager>()?.notify(SNOOZE_NOTIF_ID, notif)
    }
}
