package com.example.lockedindungeon.workers

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lockedindungeon.R
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.utils.notification.AppNotificationChannels
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SnoozeWorker @AssistedInject constructor(
    @Assisted val context : Context,
    @Assisted workerParams : WorkerParameters,
    private val datastoreRepository: AppDatastoreRepository
)
: CoroutineWorker(appContext = context, params = workerParams) {

    private val tag = "SnoozeWorker"

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
        // the channel is registered once in LockedInDungeon.onCreate, not here
        return NotificationCompat.Builder(context, AppNotificationChannels.snoozeNotificationId(applicationContext)).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle("Reactivating Blocking")
            setContentText("Snooze duration ran out, reactivating blocking")
            setAutoCancel(true)
            setPriority(NotificationCompat.PRIORITY_HIGH) // <-- Kunci Utama 2 (untuk API < 26)
            setDefaults(NotificationCompat.DEFAULT_ALL) // Aktifkan suara & getar
        }.build()
    }

    private fun publishNotif(){
        val notif = buildNotif()
        context.getSystemService<NotificationManager>()
            ?.notify(AppNotificationChannels.SNOOZE_NOTIFICATION_ID, notif)
    }
}
