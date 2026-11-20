package com.example.lockedindungeon.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.lockedindungeon.activities.BlockActivity
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import com.example.lockedindungeon.data.local.entities.BlockingType
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.data.local.repositories.AppBlockingRepository
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.data.local.repositories.AppUsageRepository
import com.example.lockedindungeon.utils.blocking.isBrowser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/**
 * Single place that decides whether something has to be blocked and shows the block screen.
 *
 * Fed by ScreenDetectionAccesibilityService (accessibility events) and
 * ForegroundPackageDetectionService (window events), so both detectors share one rule set and one
 * cache instead of duplicating the logic.
 */
@Singleton
class AppBlocker @Inject constructor(
    @ApplicationContext private val appContext : Context,
    private val blockingRepository : AppBlockingRepository,
    private val usageRepository : AppUsageRepository,
    private val datastoreRepository : AppDatastoreRepository,
    private val urlDetector : UrlDetector
) {

    companion object {
        private val tag : String = "AppBlocker"
    }

    /** Mirrors the stored state, no manual cache to keep in sync. */
    val isBlockingActive : StateFlow<Boolean> = datastoreRepository.isActive

    /** Room keeps this hot, so a newly added blocked package needs no side loading. */
    private val blockingDetails : StateFlow<List<AppBlockingDetail>> = blockingRepository.blockingDetails

    /**
     * Entry point for the accessibility service: blocks [packageName] when its rule says so, or when
     * the browser inside it opened a blocked url.
     */
    suspend fun onScreenChanged(packageName : String?, nodeInfo : AccessibilityNodeInfo?) {
        if (!isBlockingActive.value) return
        if (packageName.isNullOrEmpty()) return
        if (packageName == appContext.packageName) return

        val browser = isBrowser(packageName)
        val detail = findPackageDetail(packageName)
        if (detail == null && !browser) return

        val shouldBlockPackage = isBlocked(detail)
        val url = if (browser && nodeInfo != null) urlDetector.getUrl(nodeInfo, packageName) else null
        val shouldBlockUrl = browser && isUrlBlocked(url)

        if (!shouldBlockPackage && !shouldBlockUrl) return

        Log.d(tag, "blocking $packageName, package=$shouldBlockPackage url=$shouldBlockUrl ($url)")

        if (shouldBlockUrl && url != null && nodeInfo != null) {
            // push the browser off the blocked page and blank the url bar
            showBlocking(packageName)
            urlDetector.redirect(nodeInfo, packageName, "about:blank")
        }

        // let the app settle before the overlay steals focus
        delay(200.milliseconds)
        showBlockScreen(packageName.takeIf { !shouldBlockUrl })
    }

    /** For the window event detector, where only the url is known. */
    fun onUrlOpened(url : String) {
        if (!isBlockingActive.value) return
        if (!isUrlBlocked(url)) {
            Log.d(tag, "$url is not blocked")
            return
        }

        Log.d(tag, "blocked url opened: $url")
        showBlockScreen(null)
    }

    /** For the window event detector, where only the package is known. */
    fun onPackageOpened(packageName : String) {
        if (!isBlockingActive.value) return
        if (!isBlocked(findPackageDetail(packageName))) return

        Log.d(tag, "blocked package opened: $packageName")
        showBlockScreen(packageName)
    }

    /** Sends the browser away from the blocked page. */
    fun showBlocking(packageName : String) {
        val stopIntent = Intent(Intent.ACTION_VIEW, Uri.parse("about:blank")).apply {
            setPackage(packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        appContext.startActivity(stopIntent)
    }

    /** Turns a blocking rule into a yes/no for right now. */
    fun isBlocked(detail : AppBlockingDetail?) : Boolean {
        if (detail == null) return false
        return when (detail.blockingType) {
            BlockingType.TIMER -> isTimerExceeded(detail)
            BlockingType.BLACKLIST -> isInsideBlockedTimeframe(detail)
            BlockingType.WHITELIST -> !isInsideBlockedTimeframe(detail)
        }
    }

    fun isTimerExceeded(detail : AppBlockingDetail) : Boolean {
        val usage = usageRepository.getUsageDurationMinutes(detail.packageNameOrUrl)
        val shouldBlock = usage >= (detail.timerDurationMinute ?: 0)

        Log.d(tag, "usage=$usage timer=${detail.timerDurationMinute} shouldBlock=$shouldBlock")
        return shouldBlock
    }

    fun isInsideBlockedTimeframe(detail : AppBlockingDetail) : Boolean {
        val startHour = detail.startHour ?: return false
        val startMin = detail.startMinute ?: return false
        val endHour = detail.endHour ?: return false
        val endMin = detail.endMinute ?: return false

        val now = LocalTime.now()
        val startTime = LocalTime.of(startHour, startMin)
        val endTime = LocalTime.of(endHour, endMin)
        val isInside = now.isAfter(startTime) && now.isBefore(endTime)

        Log.d(tag, "window $startTime-$endTime now=$now inside=$isInside")
        return isInside
    }

    private fun findPackageDetail(packageName : String) : AppBlockingDetail? =
        blockingDetails.value.firstOrNull { it.packageNameOrUrl == packageName }

    private fun isUrlBlocked(url : String?) : Boolean {
        if (url.isNullOrBlank()) return false

        val detail = blockingDetails.value.firstOrNull {
            it.packageNameOrUrl == url && it.targetType == TargetType.URL
        } ?: return false

        return isBlocked(detail)
    }

    private fun showBlockScreen(packageName : String?) {
        val intent = Intent(appContext, BlockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (packageName != null) {
                putExtra("BLOCKED_PACKAGE", packageName)
            }
        }
        appContext.startActivity(intent)
    }
}
