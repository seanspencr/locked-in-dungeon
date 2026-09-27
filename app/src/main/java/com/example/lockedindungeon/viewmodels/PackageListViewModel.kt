package com.example.lockedindungeon.viewmodels

import android.content.Context
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.data.model.PackageInformationDto
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import com.example.lockedindungeon.data.local.repositories.AppListRepository
import com.example.lockedindungeon.data.local.repositories.AppBlockingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackageListState(
    val appList : List<PackageInformationDto> = listOf(),
    val urlList : List<PackageInformationDto> = listOf(),
    val isLoading : Boolean =  false,
    val selectedTargetType: TargetType = TargetType.APP,
    val urlInput: String = ""
)

@HiltViewModel
class PackageListViewModel @Inject  constructor(
    @ApplicationContext var appContext : Context,
    private val repository: AppBlockingRepository,
    private val appListRepository: AppListRepository
) : ViewModel()
{
    private val _state : MutableStateFlow<PackageListState> = MutableStateFlow(PackageListState())
    val state : StateFlow<PackageListState> = _state


    init {
        loadAppsAndBlockingData()
    }

    private fun loadAppsAndBlockingData(){
        val allApps : StateFlow<List<PackageInformationDto>> = appListRepository.appList
        val blockingDetails :  Flow<List<AppBlockingDetail>?> = repository.selectBlockingDetails() ?: flowOf(null)

        viewModelScope.launch {
            allApps.combine(
                blockingDetails,
                transform = { apps, details ->
                    Pair(apps, details)
                }
            ).collect { combined ->
                val apps = combined.first
                val details = combined.second


                val urls = details?.filter { it.targetType == TargetType.URL }?.map {
                    PackageInformationDto(it.packageNameOrUrl, it.displayName, true)
                } ?: listOf()

//                mark apps yang ada kedaftar di repo sebagai blocked
                details?.forEach { detail ->
                    apps.find { app ->
                        app.packageName == detail.packageNameOrUrl
                    }?.isBlocked = true
                }

                _state.value = _state.value.copy(
                    appList = apps.toList(),
                    urlList = urls,
                    isLoading = false
                )
            }
        }
    }


    fun onTargetTypeChange(targetType: TargetType) {
        _state.value = _state.value.copy(selectedTargetType = targetType)
    }

    fun onUrlInputChange(input: String) {
        _state.value = _state.value.copy(urlInput = input)
    }


}
