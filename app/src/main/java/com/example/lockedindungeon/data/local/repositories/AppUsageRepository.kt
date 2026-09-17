package com.example.lockedindungeon.data.local.repositories

import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

data class UsageStatsMinute(
    val packageName : String,
    val usageMinute : Int
)

class AppUsageRepository @Inject constructor(
    @ApplicationContext private val appContext : Context
) {


    public fun getUsageDurationMinutes(packageName : String) : Int{
        val stats = getUsageStats()

        return stats?.first { it.packageName == packageName }?.usageMinute ?: 0
    }

    public fun getUsageStats(): List<UsageStatsMinute>? {
        val mUsageStatsManager =
            this.appContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val startTimeMillis = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val stats = mUsageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTimeMillis,
            System.currentTimeMillis()
        )


        return stats?.groupBy {
            it -> it.packageName
        }?.map {
            (packagename, usage) ->
            UsageStatsMinute(
                packageName = packagename,
                usageMinute = ((usage.sumOf { it -> it.totalTimeInForeground }?: 0) / 60000).toInt()
            )
        }
    }


}