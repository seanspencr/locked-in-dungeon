package com.example.lockedindungeon

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.lockedindungeon.utils.notification.AppNotificationChannels
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LockedInDungeon : Application(), Configuration.Provider {


    companion object {
        public const val TAG = "LockedInDungeon"
    }
    @Inject lateinit var workerFactory: HiltWorkerFactory

    /**
     * Token/FIS callbacks land here instead of the main thread, so a busy UI thread cannot make a
     * completed task look like a callback that never fired.
     */


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // runs on every process start, including service-only background starts where no activity
        // exists yet, so the channels are always there before anything posts on them
        AppNotificationChannels.createAll(this)

        // Registers this installation with FCM. The result is not the id, the id arrives in
        // FirebaseMessagingService.onRegistered(installationId).




//        logInstallationId()
    }

    /**
     * Debug helper: prints the Firebase Installation ID, filter logcat on tag "LockedInDungeon".
     *
     * The FCM *token* API is disabled here on purpose: `firebase_messaging_installation_id_enabled`
     * is set to true in the manifest, so [FirebaseMessaging.getToken] returns a failed task with
     * "API disabled". In that mode the installation id (FID) is the handle used to target this app
     * instance, and it is the same value handed to
     * [com.example.lockedindungeon.services.FirebaseMessagingServiceImpl.onRegistered].
     */
//    private fun logInstallationId() {
//        FirebaseInstallations.getInstance().id.addOnCompleteListener(tokenExecutor) { task ->
//            if (task.isSuccessful) {
//                Log.d(TAG, "Firebase installation id: ${task.result}")
//            } else {
//                Log.e(TAG, "Firebase installation id failed", task.exception)
//            }
//        }
//    }
}
