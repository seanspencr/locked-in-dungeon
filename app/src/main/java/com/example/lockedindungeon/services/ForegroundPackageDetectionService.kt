package com.example.lockedindungeon.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.lockedindungeon.data.local.repositories.AppUsageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


// Service yang ngequery window event, lalu lempar ke appblocker
class ForegroundPackageDetectionService @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    @ApplicationContext private val applicationContext: ApplicationContext
): Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var latestCheckMillis : Long = 0;


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun startDetection(){
        serviceScope.launch {
            while (true){
                latestCheckMillis =
                appUsageRepository.queryWindowEvent()
//                check every 1 second
                delay(1000.milliseconds)
            }
        }
    }


}