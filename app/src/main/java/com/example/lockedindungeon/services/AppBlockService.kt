package com.example.lockedindungeon.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.lockedindungeon.activities.BlockActivity
import com.example.lockedindungeon.data.local.entities.BlockingType
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.data.local.repositories.AppStateRepository
import com.example.lockedindungeon.data.local.repositories.AppUsageRepository
import com.example.lockedindungeon.data.local.repositories.PackageBlockingLocalRepository
import com.example.lockedindungeon.feature.UrlDetector
import com.example.lockedindungeon.utils.RedirectTokenManager
import com.example.lockedindungeon.utils.isBrowser
import dagger.hilt.EntryPoints
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalTime
import kotlin.time.Duration.Companion.milliseconds


class AppBlockService: AccessibilityService() {

    lateinit var blockingRepo: PackageBlockingLocalRepository
    lateinit var usageRepo: AppUsageRepository
    lateinit var stateRepository: AppStateRepository
    lateinit var urlDetector: UrlDetector

//    IDK WHAT IS THIS tapi katanya karena accessibility service ga ngikut lifecycle yg bisa diurus sm hilt jadi gabisa
    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(SingletonComponent::class)
    interface AppBlockServiceEntryPoint {
        fun getBlockingRepo(): PackageBlockingLocalRepository
        fun getUsageRepo(): AppUsageRepository
        fun getStateRepository(): AppStateRepository
    }

    override fun onCreate() {
        val entryPoint = EntryPoints.get(
            applicationContext,
            AppBlockServiceEntryPoint::class.java
        )
        blockingRepo = entryPoint.getBlockingRepo()
        usageRepo = entryPoint.getUsageRepo()
        stateRepository = entryPoint.getStateRepository()
        urlDetector = UrlDetector()
        super.onCreate()
    }

    private val blockingDetailCache : MutableList<AppBlockingDetail> = mutableListOf()
    companion object {
        val tag : String = "AppBlockService"
    }


    suspend fun registerBlockedPackages(){
        blockingRepo.selectBlockingDetails()?.collectLatest { blocks ->
            // 1. Clear the old cache states completely
            blockingDetailCache.clear()

            // 2. Repopulate with the fresh database snapshot
            blocks?.forEach { detail ->
                addMonitoredPackage(detail.packageNameOrUrl)
                blockingDetailCache.add(detail)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

//        Log.d(tag, "Event detected");
        CoroutineScope(Dispatchers.IO).launch {
            stateRepository.isActive.collect {
                isActive ->
                if(!isActive) return@collect
                if(event?.packageName == applicationContext.packageName) return@collect

                event?.let {
                        ev ->
                    //  --------  handle blocking by package name
                    if(!intArrayOf(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.TYPE_WINDOWS_CHANGED, AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED).contains(ev.eventType)) return@collect

//                    Log.d(tag, "window state changed ${ev.packageName}")

                    val detail : AppBlockingDetail? = blockingDetailCache.find { it.packageNameOrUrl ==  ev.packageName}
                    if(detail == null && !isBrowser(ev.packageName.toString())) {
//                        Log.d(tag, "Package from ${ev.packageName} dteected, but not blocked")
                        return@collect
                    }

                    val shouldBlockPackage : Boolean = isEventShouldBeBlocked(detail)

                    //  --------  handle blocking by url name
                    var shouldBlockUrl : Boolean = false
                    Log.d(tag, "packageName : ${ev.packageName.toString()}")
                    if(isBrowser(ev.packageName.toString())){
                        Log.d(tag, "browser detected")
                        shouldBlockUrl = isUrlShouldBlocked(ev.source, ev.packageName.toString())
                    }

                    if(shouldBlockPackage || shouldBlockUrl){
                        val token = RedirectTokenManager.generateToken()
                        val blockUri = Uri.parse("content://com.example.lockedindungeon/files/block/block-page.html?token=$token")

                        if (shouldBlockUrl && ev.source != null) {
                            // Force the browser to navigate away from the blocked page immediately
                            val stopIntent = Intent(Intent.ACTION_VIEW, Uri.parse("about:blank")).apply {
                                setPackage(ev.packageName.toString())
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(stopIntent)
                            
                            // Visually update the URL bar
                            urlDetector.redirect(ev.source!!, ev.packageName.toString(), blockUri.toString())
                        }

                        val intent = if (shouldBlockUrl) {
                            Intent(applicationContext, BlockActivity::class.java).apply {
                                action = Intent.ACTION_VIEW
                                data = blockUri
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                        } else {
                            Intent(applicationContext, BlockActivity::class.java).apply {
                                data = blockUri
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("BLOCKED_PACKAGE", ev.packageName)
                            }
                        }

                        // Small delay or back action to interrupt any remaining browser activity
                        withContext(Dispatchers.Default){
                            run {
                                performGlobalAction(GLOBAL_ACTION_BACK)
                                delay(200.milliseconds)
                                startActivity(intent)
                            }
                        }

                        return@collect
                    }



                }
            }
        }
    }

    private fun isUrlShouldBlocked(nodeInfo : AccessibilityNodeInfo?, packageName : String): Boolean {
        if(nodeInfo == null) return false


        val url : String = urlDetector.getUrl(nodeInfo, packageName)

        val detail = blockingDetailCache.find { it.packageNameOrUrl == url && it.targetType == TargetType.URL }
        if (detail != null) {
            return isEventShouldBeBlocked(detail)
        }

        return false
    }


    private fun isEventShouldBeBlocked(detail: AppBlockingDetail?): Boolean {
        if(detail == null) return false
        return when (detail.blockingType) {
            BlockingType.TIMER -> isTimerExceeded(detail)
            BlockingType.BLACKLIST -> isInsideBlockedTimeframe(detail)
            BlockingType.WHITELIST -> isInsideBlockedTimeframe(detail)
            else -> {
                Log.e(tag, "Unknown blocking type")
                false
            }
    }
    }

    fun isTimerExceeded(appBlockingDetail: AppBlockingDetail) : Boolean{
        val usage = usageRepo.getUsageDurationMinutes(appBlockingDetail.packageNameOrUrl)
        val shouldBlock = usage >= (appBlockingDetail.timerDurationMinute ?: 0)

        Log.d(tag, "Usage : ${usage}, timer : ${appBlockingDetail.timerDurationMinute}, shouldBlock : ${shouldBlock}")
        return shouldBlock
    }

    fun isInsideBlockedTimeframe(appBlockingDetail: AppBlockingDetail) : Boolean{
        val startHour = appBlockingDetail.startHour ?: return false
        val startMin = appBlockingDetail.startMinute ?: return false
        val endHour = appBlockingDetail.endHour ?: return false
        val endMin = appBlockingDetail.endMinute ?: return false

        val now = LocalTime.now()
        val startTime = LocalTime.of(startHour, startMin)
        val endTime = LocalTime.of(endHour, endMin)

        val isInside = now.isAfter(startTime) && now.isBefore(endTime)

        Log.d(tag, "startTime : ${startTime}, endTime : ${endTime}, now : ${now}, isInside : ${isInside}")
        return when (appBlockingDetail.blockingType) {
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