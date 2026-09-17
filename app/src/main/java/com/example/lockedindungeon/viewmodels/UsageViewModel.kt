package com.example.lockedindungeon.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.lockedindungeon.data.local.repositories.AppUsageRepository
import com.example.lockedindungeon.data.local.repositories.UsageStatsMinute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


data class UsageViewmodelState(
    public val appUsage : List<UsageStatsMinute> = listOf()
)

@HiltViewModel
class UsageViewModel @Inject constructor (
    val appUsageRepository : AppUsageRepository
) : ViewModel(){

    private val _state : MutableState<UsageViewmodelState> = mutableStateOf(UsageViewmodelState(listOf()))
    public val state : State<UsageViewmodelState> = _state

    init {
        appUsageRepository.getUsageStats()?.let {
            _state.value = _state.value.copy(appUsage = it)
        }
    }





}