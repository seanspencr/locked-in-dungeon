package com.example.lockedindungeon.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.example.lockedindungeon.R

/**
 * The notification channels the app posts on.
 *
 * Channels belong to the system, not to a process or an activity, so they are registered once in
 * [com.example.lockedindungeon.LockedInDungeon.onCreate] and every poster (FCM service, workers)
 * only has to reference the ids below. Registering again is a no-op.
 */
object AppNotificationChannels {

//    notif name ini buat kata2 yg muncul di settings user, ketika app > permission : nnti kelisting notif name ini, daftarin sekali aja wktu create notification channel
    const val FRIEND_NOTIFICATION_NAME = "Friend"
    const val BLOCKING_NOTIFICATION_NAME = "AppBlocker Status"
    const val SNOOZE_NAME = "Snooze Notification"

//  channelId (INT)  ini buat ketika .notify() dia masuk kemana (misal intnya sama berarti ke overwrite contentnya tiap dinotify)
    const val SNOOZE_NOTIFICATION_ID = 0
    const val FRIEND_NOTIFICATION_ID = 1
    const val BLOCKING_NOTIFICATION_ID = 2


//    notif id (String) ini gunanya buat identifier secara system utk notif channelnya, nanti ketika NotificationCompat.Builder(..., channelId) <- karena si channelId ini juga mapped ke string yang muncul di settings user
    fun friendNotificationId(context: Context) : String =
        context.getString(R.string.default_notification_channel_id)

    fun blockingNotificationId(context : Context) : String{
        return context.getString(R.string.blocking_notification_channel_id)
    }

    fun snoozeNotificationId(context: Context) : String {
        return context.getString(R.string.snooze_notification_channel_id)
    }
    fun createAll(context: Context) {
        val notificationManager = context.getSystemService<NotificationManager>() ?: return

        notificationManager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    friendNotificationId(context),
                    FRIEND_NOTIFICATION_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Nudges from friends while your block is running"
                },
                NotificationChannel(
                    blockingNotificationId(context),
                    BLOCKING_NOTIFICATION_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Tell you whether the blocking is running or interrupted"
                },
                NotificationChannel(
                    snoozeNotificationId(context),
                    SNOOZE_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Tells you the snooze ran out and that blocking is back on"
                },
            )
        )
    }
}
