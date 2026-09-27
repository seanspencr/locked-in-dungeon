package com.example.lockedindungeon.data.local.repositories

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.os.Process
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.data.model.PackageInformationDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppListRepository @Inject constructor(
     @ApplicationContext val appContext :  Context
) {
    private val tag: String = "AppListRepository"
    private val _appList : MutableStateFlow<List<PackageInformationDto>> = MutableStateFlow(listOf())
    public val appList : StateFlow<List<PackageInformationDto>> = _appList


    init {
        loadApps()
    }

    private fun loadApps() {
//        .launch brarti jalanin sesuatu di dlm coroutine, yang bisa switch thread itu cuma bisa dilakukan klo di dlm coroutine

        val launcherApps =
            appContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val packageManager = appContext.packageManager
        val currentPackage = appContext.packageName
        val result = mutableListOf<PackageInformationDto>()

        launcherApps.profiles.forEach { profile ->
            launcherApps.getActivityList(null, profile)
                .map { it.applicationInfo }
                .filter { it.packageName != currentPackage }
                .forEach { appInfo ->
                    val profileType =
                        if (profile == Process.myUserHandle()) "" else "(Work)"
                    val appLabel = appInfo.loadLabel(packageManager).toString()
                    val displayName = "$appLabel $profileType"
                    val icon : Bitmap = appContext.packageManager.getApplicationIcon(appInfo.packageName).toBitmap(120, 120 )
                    result.add(PackageInformationDto(appInfo.packageName, displayName, icon = icon))
                }


        }

        _appList.value = result.distinctBy { it -> it.packageName }
    }

    public fun queryPackageInformation(packageName : String) : PackageInformationDto? {
        return appList.value.firstOrNull { it.packageName == packageName }
    }
}