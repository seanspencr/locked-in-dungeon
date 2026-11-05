package com.example.lockedindungeon.utils

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

    const val FRIEND_POKES_NAME = "Friend pokes"

    const val SNOOZE_ID = "snooze_notif_channel_v2"
    const val SNOOZE_NAME = "Snooze Notification"

    /** Notification id used for the "blocking is back on" notification. */
    const val SNOOZE_NOTIFICATION_ID = 0
    const val FRIEND_POKES_NAME_ID = 1

    /**
     * Read from `default_notification_channel_id`, so the id used in code can never drift away from
     * the `com.google.firebase.messaging.default_notification_channel_id` manifest meta-data that
     * FCM reads for notifications it displays itself.
     */
    fun friendPokesId(context: Context) : String =
        context.getString(R.string.default_notification_channel_id)

    fun createAll(context: Context) {
        val notificationManager = context.getSystemService<NotificationManager>() ?: return

        notificationManager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    friendPokesId(context),
                    FRIEND_POKES_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Nudges from friends while your block is running"
                },
                NotificationChannel(
                    SNOOZE_ID,
                    SNOOZE_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Tells you the snooze ran out and that blocking is back on"
                },
            )
        )
    }
}
