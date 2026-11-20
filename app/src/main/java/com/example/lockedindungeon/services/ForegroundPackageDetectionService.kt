package com.example.lockedindungeon.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import com.example.lockedindungeon.R
import com.example.lockedindungeon.common.AppBlocker
import com.example.lockedindungeon.data.local.repositories.AppUsageRepository
import com.example.lockedindungeon.utils.blocking.toUsageEventName
import com.example.lockedindungeon.utils.notification.AppNotificationChannels
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


// Service yang ngequery window event, lalu lempar ke appblocker
@AndroidEntryPoint
class ForegroundPackageDetectionService : Service() {

    @Inject
    lateinit var appUsageRepository: AppUsageRepository

    @Inject
    @ApplicationContext
    lateinit var appContext: Context // Renamed to avoid clashing with Service's getApplicationContext()

    @Inject
    lateinit var appBlocker: AppBlocker
    private val TAG = "ForegroundPackageDetectionService"

    private val packageStackStatus : MutableMap<String, Int> = mutableMapOf();

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var latestCheckMillis = System.currentTimeMillis() - 2000L

    companion object {
        private val _isRunning : MutableStateFlow<Boolean> = MutableStateFlow<Boolean>(false)
        public val isRunning : StateFlow<Boolean> = _isRunning
    }



    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun buildNotification() : Notification {
        return Notification.Builder(applicationContext, AppNotificationChannels.blockingNotificationId(appContext)).apply {
            setContentTitle("App Monitoring Started")
            setContentText("Locked in Dungeon is monitoring your phone right now")
            setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            setOnlyAlertOnce(true)
        }.build()
    }

    private fun publishNotification(){
        val notif = buildNotification()
        getSystemService(NotificationManager::class.java).notify(AppNotificationChannels.BLOCKING_NOTIFICATION_ID,  notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(AppNotificationChannels.BLOCKING_NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_SPECIAL_USE)

        startDetection()

        return START_STICKY
    }

    fun startDetection(){
        _isRunning.value = true
        serviceScope.launch {
            while (isActive) { // Use isActive instead of while(true) so it cancels cleanly
                val now = System.currentTimeMillis()
                val events = appUsageRepository.queryWindowEvent(latestCheckMillis, now)
                latestCheckMillis = now

                events?.let {
                    var packageNames = getForegroundApp(it)
                    appBlocker.onPackagesOpened(packageNames);
                }

                delay(1500.milliseconds)
            }
        }
    }

    fun getForegroundApp(events : UsageEvents) : List<String>{
        events.let {
            ev ->
            while (ev.hasNextEvent()){
                var event : UsageEvents.Event = UsageEvents.Event();
                ev.getNextEvent(event);
                Log.d(TAG, "Queried event : ${event.packageName}, event type ${event.eventType.toUsageEventName()}")

                if(event.eventType ==  UsageEvents.Event.ACTIVITY_STOPPED){
                    packageStackStatus.remove(event.packageName)
                }else{
                    packageStackStatus[event.packageName] = event.eventType
                }

            }


            return packageStackStatus.keys.filter {
                it -> packageStackStatus[it] == UsageEvents.Event.ACTIVITY_RESUMED
            }
        }
    }




}