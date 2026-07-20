package com.example.lockedindungeon.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.lockedindungeon.activities.BlockActivity
import com.example.lockedindungeon.data.local.entities.BlockingType
import com.example.lockedindungeon.data.local.entities.PackageBlockingDetail
import com.example.lockedindungeon.data.local.repositories.AppUsageRepository
import com.example.lockedindungeon.data.local.repositories.PackageBlockingLocalRepository
import dagger.hilt.EntryPoints
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime


class AppBlockService: AccessibilityService() {

    lateinit var blockingRepo: PackageBlockingLocalRepository
    lateinit var usageRepo: AppUsageRepository

//    IDK WHAT IS THIS tapi katanya karena accessibility service ga ngikut lifecycle yg bisa diurus sm hilt jadi gabisa
    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(SingletonComponent::class)
    interface AppBlockServiceEntryPoint {
        fun getBlockingRepo(): PackageBlockingLocalRepository
        fun getUsageRepo(): AppUsageRepository
    }

    override fun onCreate() {
        val entryPoint = EntryPoints.get(
            applicationContext,
            AppBlockServiceEntryPoint::class.java
        )
        blockingRepo = entryPoint.getBlockingRepo()
        usageRepo = entryPoint.getUsageRepo()
        super.onCreate()
    }

    private val blockingDetailCache : MutableList<PackageBlockingDetail> = mutableListOf()
    companion object {
        val tag : String = "AppBlockService"
    }


    suspend fun registerBlockedPackages(){
        blockingRepo.selectBlockingDetails()?.collectLatest { blocks ->
            // 1. Clear the old cache states completely
            blockingDetailCache.clear()

            // 2. Repopulate with the fresh database snapshot
            blocks?.forEach { detail ->
                addMonitoredPackage(detail.packageName)
                blockingDetailCache.add(detail)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        Log.d(tag, "Event detected");
        event?.let {
            ev ->
           if(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED != ev.eventType) return

           Log.d(tag, "window state changed ${ev.packageName}")

            val detail : PackageBlockingDetail? = blockingDetailCache.find { it.packageName ==  ev.packageName}
            if(detail == null) {
                Log.d(tag, "Package from ${ev.packageName} dteected, but not blocked")
                return
            }


            val shouldBlock : Boolean = when(detail.blockingType){
                BlockingType.TIMER -> isTimerExceeded(detail)
                BlockingType.BLACKLIST -> isInsideBlockedTimeframe(detail)
                BlockingType.WHITELIST -> isInsideBlockedTimeframe(detail)
                else -> {
                    Log.e(tag, "Unknown blocking type")
                    false
                }
            }

            if(shouldBlock){
//                start activtity and show webview
                val intent = Intent(this, BlockActivity::class.java).apply{
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                intent.putExtra("BLOCKED_PACKAGE", ev.packageName)
                startActivity(intent)
            }

        }
    }

    fun isTimerExceeded(packageBlockingDetail: PackageBlockingDetail) : Boolean{
        val usage = usageRepo.getUsageDurationMinutes(packageBlockingDetail.packageName)
        val shouldBlock = usage >= (packageBlockingDetail.timerDurationMinute ?: 0)

        Log.d(tag, "Usage : ${usage}, timer : ${packageBlockingDetail.timerDurationMinute}, shouldBlock : ${shouldBlock}")
        return shouldBlock
    }

    fun isInsideBlockedTimeframe(packageBlockingDetail: PackageBlockingDetail) : Boolean{
        val startHour = packageBlockingDetail.startHour ?: return false
        val startMin = packageBlockingDetail.startMinute ?: return false
        val endHour = packageBlockingDetail.endHour ?: return false
        val endMin = packageBlockingDetail.endMinute ?: return false

        val now = LocalTime.now()
        val startTime = LocalTime.of(startHour, startMin)
        val endTime = LocalTime.of(endHour, endMin)

        val isInside = now.isAfter(startTime) && now.isBefore(endTime)

        Log.d(tag, "startTime : ${startTime}, endTime : ${endTime}, now : ${now}, isInside : ${isInside}")
        return when (packageBlockingDetail.blockingType) {
            BlockingType.BLACKLIST -> isInside  // Block if CURRENTLY within bounds
            BlockingType.WHITELIST -> !isInside // Block if CURRENTLY outside bounds
            else -> false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(tag, "Service started")
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO){
                registerBlockedPackages()
            }

        }
    }

    override fun onInterrupt() {
        Log.d(tag, "Service Interrupted")
    }

    fun updateMonitoredPackages(packages: Array<String>?) {
        var info = serviceInfo
        if (info == null) {
            info = AccessibilityServiceInfo()
        }
        info.packageNames = packages
        setServiceInfo(info)
        Log.d(tag, "Monitored package" + serviceInfo.packageNames.joinToString(","))
    }

    fun addMonitoredPackage(packageName : String){
        var monitored = serviceInfo.packageNames
        if(!monitored.contains(packageName)){
            monitored += packageName
        }
        updateMonitoredPackages(monitored)
    }


    fun removeMonitoredPackage(packageName : String){
        var monitored = serviceInfo.packageNames
        var filtered = monitored.filter { it -> it != packageName }
        updateMonitoredPackages(filtered.toTypedArray())
    }


}