package com.example.lockedindungeon.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent


class AppBlockService : AccessibilityService() {

    companion object {
        val tag : String = "AppBlockService"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        Log.d(tag, "Event detected");
        event?.let {
           if(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == it.eventType){
               Log.d(tag, "window state changed ${it.packageName}")
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

    fun updateMonitoredPackages(packages: Array<String>?) {
        var info = serviceInfo
        if (info == null) {
            info = AccessibilityServiceInfo()
        }
        info.packageNames = packages
        setServiceInfo(info)
    }

    fun addMonitoredPackage(packageName : String){
        var monitored = serviceInfo.packageNames
        monitored += packageName
        updateMonitoredPackages(monitored)
    }

    fun removeMonitoredPackage(packageName : String){
        var monitored = serviceInfo.packageNames
        var filtered = monitored.filter { it -> it != packageName }
        updateMonitoredPackages(filtered.toTypedArray())
    }


}