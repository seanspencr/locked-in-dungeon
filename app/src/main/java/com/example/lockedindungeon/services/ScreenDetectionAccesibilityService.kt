package com.example.lockedindungeon.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.lockedindungeon.common.AppBlocker
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import dagger.hilt.EntryPoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


/**
 * Feeds accessibility events to [AppBlocker], which owns the blocking rules and the block screen.
 *
 * The service only does what an AccessibilityService alone can do: receive events and notice when it
 * gets unbound. Which packages wake it up is configured in `res/xml/accessibilityservice.xml`.
 */
class ScreenDetectionAccesibilityService : AccessibilityService() {

    private lateinit var stateRepository : AppDatastoreRepository
    private lateinit var appBlocker : AppBlocker
    private lateinit var serviceScope : CoroutineScope

//    IDK WHAT IS THIS tapi katanya karena accessibility service ga ngikut lifecycle yg bisa diurus sm hilt jadi gabisa
    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface AppBlockServiceEntryPoint {
        fun getStateRepository() : AppDatastoreRepository
        fun getAppBlocker() : AppBlocker
    }

    companion object {
        val tag : String = "AppBlockService"

        /** Event types worth evaluating, everything else is noise for the blocker. */
        private val WATCHED_EVENTS = intArrayOf(
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        )
    }

    override fun onCreate() {
        val entryPoint = EntryPoints.get(
            applicationContext,
            AppBlockServiceEntryPoint::class.java
        )
        stateRepository = entryPoint.getStateRepository()
        appBlocker = entryPoint.getAppBlocker()
        serviceScope = CoroutineScope(Dispatchers.IO)
        super.onCreate()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(tag, "Service started")
    }

    override fun onAccessibilityEvent(event : AccessibilityEvent?) {
        val ev = event ?: return

        // cheap checks first, the blocking decision itself runs off the main thread
        if (!appBlocker.isBlockingActive.value) return
        if (ev.packageName == applicationContext.packageName) return
        if (ev.eventType !in WATCHED_EVENTS) return

        val packageName = ev.packageName?.toString() ?: return
        val source = ev.source

        serviceScope.launch {
            appBlocker.onScreenChanged(packageName, source)
        }
    }

    override fun onInterrupt() {
        Log.d(tag, "Service interrupted")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onUnbind(intent : Intent?) : Boolean {
        serviceScope.launch {
            stateRepository.setActive(false)
        }
        return super.onUnbind(intent)
    }
}
