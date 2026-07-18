package com.example.lockedindungeon.viewmodels

import android.app.Application
import android.content.Context
import android.content.pm.LauncherApps
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.lockedindungeon.data.model.PackageInformationDto
import android.os.Process

data class PackageListState(
    val appList : List<PackageInformationDto> = listOf()
)
class PackageListViewModel(application : Application) : AndroidViewModel(application)
{
    private val _state : MutableState<PackageListState> = mutableStateOf(PackageListState())
    val state : State<PackageListState> = _state

    init {
        val launcherApps : LauncherApps = application.applicationContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val profiles = launcherApps.profiles
        val packageManager = application.applicationContext.packageManager
        val currentPackage = application.applicationContext.packageName


        var allApps = mutableListOf<PackageInformationDto>()
        profiles.forEach{profile ->
            launcherApps.getActivityList(null, profile).map {
                it.applicationInfo
            }.filter {
                it.packageName != currentPackage
            }.forEach {
                val profileType = if (profile == Process.myUserHandle()) "" else "(Work)"
                val appLabel = it.loadLabel(packageManager).toString()
                val displayName = "$appLabel $profileType"

                allApps.add(
                    PackageInformationDto(it.packageName, displayName)
                )
            }
        }
        _state.value = _state.value.copy(appList = allApps)
    }


}