package com.example.lockedindungeon.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.data.local.repositories.AppSettings
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.data.remote.UserRepository
import com.example.lockedindungeon.dto.ModifySettingsRequest
import com.example.lockedindungeon.dto.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsScreenState(
    val appSettings : AppSettings = AppSettings()
)

@HiltViewModel
class SettingsViewmodel @Inject constructor(
    private val dataStoreRepository : AppDatastoreRepository,
    private val userRepository : UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _state : MutableStateFlow<SettingsScreenState> = MutableStateFlow(SettingsScreenState())
    val state : StateFlow<SettingsScreenState> = _state

    init {
        viewModelScope.launch {
            dataStoreRepository.settings.collect {
                settings -> _state.value = _state.value.copy(appSettings = settings)
            }
        }
        refreshFromRemote()
    }

    fun saveDisableAttemptWord(newVal : String) {
        applyLocal { it.copy(disableAttemptWord = newVal) }
    }

    fun saveShowStats(newVal : Boolean) {
        applyLocal { it.copy(showStats = newVal) }
        pushToRemote()
    }

    fun saveShowBlockList(newVal : Boolean) {
        applyLocal { it.copy(showBlockList = newVal) }
        pushToRemote()
    }

    /** Local DataStore is the source of truth for the UI, the remote call happens after it. */
    private fun applyLocal(transform : (AppSettings) -> AppSettings) {
        val updated = transform(_state.value.appSettings)
        _state.value = _state.value.copy(appSettings = updated)
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.setSettings(updated)
        }
    }

    private fun pushToRemote() {
        val settings = _state.value.appSettings
        // the backend spells the block list toggle differently: showBlockList -> showBlockingList
        val request = ModifySettingsRequest(
            showBlockingList = settings.showBlockList,
            showStats = settings.showStats
        )

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = userRepository.updateSettings(request)) {
                is ApiResult.Success<UserSettings?> -> Log.d(TAG, "settings pushed to backend")
                is ApiResult.Error -> Log.d(TAG, "updateSettings failed: ${result.message}")
            }
        }
    }

    /** Pulls the toggles once, so the switches reflect the account and not just this device. */
    private fun refreshFromRemote() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = userRepository.getUserSettings()) {
                is ApiResult.Success<UserSettings?> -> {
                    val remote = result.data
                    if (remote == null) {
                        Log.d(TAG, "getUserSettings returned no body")
                        return@launch
                    }

                    val merged = _state.value.appSettings.copy(
                        showStats = remote.showStats ?: _state.value.appSettings.showStats,
                        showBlockList = remote.showBlocklist ?: _state.value.appSettings.showBlockList
                    )
                    _state.value = _state.value.copy(appSettings = merged)
                    dataStoreRepository.setSettings(merged)
                }
                is ApiResult.Error -> Log.d(TAG, "getUserSettings failed: ${result.message}")
            }
        }
    }
}
