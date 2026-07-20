package com.example.lockedindungeon.data.local.repositories

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject


class AppUsageRepository @Inject constructor(
    @ApplicationContext private val appContext : Context
) {

    public fun getUsageDurationMinutes(packageName : String) : Int{
        val mUsageStatsManager = this.appContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val startTimeMillis = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // The OS constructs a structural map aggregated by package key names
        val stats = mUsageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTimeMillis,
            System.currentTimeMillis()
        )

        val totalTimeMs = stats.filter { it.packageName == packageName }.sumOf { it -> it.totalTimeInForeground } ?: 0L
        return (totalTimeMs / 60000).toInt()
    }

}