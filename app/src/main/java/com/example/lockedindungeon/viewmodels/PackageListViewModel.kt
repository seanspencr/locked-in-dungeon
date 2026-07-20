package com.example.lockedindungeon.viewmodels

import android.app.Application
import android.content.Context
import android.content.pm.LauncherApps
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.lockedindungeon.data.model.PackageInformationDto
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.repositories.PackageBlockingLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PackageListState(
    val appList : List<PackageInformationDto> = listOf(),
    val isLoading : Boolean =  false
)

@HiltViewModel
class PackageListViewModel @Inject  constructor(
    @ApplicationContext var appContext : Context,
    private val repository: PackageBlockingLocalRepository
) : ViewModel()
{
    private val _state : MutableState<PackageListState> = mutableStateOf(PackageListState())
    val state : State<PackageListState> = _state


    init {
        loadApps()
    }


    private fun loadApps() {
//        .launch brarti jalanin sesuatu di dlm coroutine, yang bisa switch thread itu cuma bisa dilakukan klo di dlm coroutine
        viewModelScope.launch {
//            pake Dispatcher.Default = background thread yg cpu heavy, buat parsing gtgt
//            Dispatchers.Main = UI thread
//            Dispatcher.IO lebih gede dari default, biasa buat network call

            val allApps = withContext(Dispatchers.Default) {
                _state.value = _state.value.copy(
                    isLoading = true
                )

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
                            result.add(PackageInformationDto(appInfo.packageName, displayName))
                        }
                }
                result
            }

            repository.selectBlockingDetail()?.collect { details ->
                details?.forEach {
                    allApps.find { app ->
                        app.packageName == it.packageName
                    }?.isBlocked = true


                    _state.value = _state.value.copy(appList = allApps, isLoading = false)
                }

                _state.value = _state.value.copy(appList = allApps, isLoading = false)
            }


        }
    }
}
