package com.example.lockedindungeon.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AppBlockService : AccessibilityService() {

    companion object {
        val tag : String = "AppBlockService"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

//        Log.d(tag, "Event detected");
        event?.let {
           if(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == it.eventType){
               Log.d(tag, "window state changed")
           }
        }

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(tag, "Service started")
    }

    override fun onInterrupt() {
        Log.d(tag, "Service Interrupted")
    }
}