package com.example.lockedindungeon.data.local.repositories

import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.lockedindungeon.data.model.PackageInformationDto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

data class UsageStatsMinute(
    val packageInfo: PackageInformationDto,
    val usageMinute : Int
)

class AppUsageRepository @Inject constructor(
    private val appListRepository: AppListRepository,
    @ApplicationContext private val appContext : Context
) {


    public fun getUsageDurationMinutes(packageName : String) : Int{
        val stats = getUsageStats()

        return stats?.first { it.packageInfo.packageName == packageName }?.usageMinute ?: 0
    }

    public fun getUsageStats(): List<UsageStatsMinute>? {
        val usageStatsManager =
            this.appContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val startTimeMillis = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTimeMillis,
            System.currentTimeMillis()
        )


        return stats?.groupBy {
            it -> it.packageName
        }?.map {
            (packagename, usage) ->
            val packageInfo = appListRepository.queryPackageInformation(packagename)
            UsageStatsMinute(
                packageInfo = PackageInformationDto(
                    packagename,
                    packageInfo?.displayName ?: packagename,
                    icon = packageInfo?.icon,
                    isBlocked = packageInfo?.isBlocked ?: false
                ),
                usageMinute = ((usage.sumOf { it -> it.totalTimeInForeground }?: 0) / 60000).toInt()
            )
        }
    }


}