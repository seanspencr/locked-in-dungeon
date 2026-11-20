package com.example.lockedindungeon.utils.blocking

import android.app.usage.UsageEvents

fun Int.toUsageEventName(): String = when (this) {
    UsageEvents.Event.NONE -> "NONE"
    UsageEvents.Event.ACTIVITY_RESUMED -> "ACTIVITY_RESUMED"       // 1
    UsageEvents.Event.ACTIVITY_PAUSED -> "ACTIVITY_PAUSED"         // 2
    UsageEvents.Event.CONFIGURATION_CHANGE -> "CONFIGURATION_CHANGE" // 5
    UsageEvents.Event.USER_INTERACTION -> "USER_INTERACTION"       // 7
    UsageEvents.Event.SHORTCUT_INVOCATION -> "SHORTCUT_INVOCATION" // 8
    UsageEvents.Event.STANDBY_BUCKET_CHANGED -> "STANDBY_BUCKET_CHANGED" // 11
    UsageEvents.Event.SCREEN_INTERACTIVE -> "SCREEN_INTERACTIVE"   // 15
    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> "SCREEN_NON_INTERACTIVE" // 16
    UsageEvents.Event.KEYGUARD_SHOWN -> "KEYGUARD_SHOWN"           // 17
    UsageEvents.Event.KEYGUARD_HIDDEN -> "KEYGUARD_HIDDEN"         // 18
    UsageEvents.Event.FOREGROUND_SERVICE_START -> "FOREGROUND_SERVICE_START" // 19
    UsageEvents.Event.FOREGROUND_SERVICE_STOP -> "FOREGROUND_SERVICE_STOP" // 20
    UsageEvents.Event.ACTIVITY_STOPPED -> "ACTIVITY_STOPPED"       // 23
    else -> "UNKNOWN($this)"
}