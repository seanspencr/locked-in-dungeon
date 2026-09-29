package com.example.lockedindungeon.viewmodels

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.data.local.repositories.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsScreenState(
    val appSettings : AppSettings = AppSettings()
)

@HiltViewModel
class SettingsViewmodel @Inject constructor(
    @ApplicationContext private val appContext : Context,
    private val dataStoreRepository : AppDatastoreRepository
) : ViewModel() {

    private val _state : MutableStateFlow<SettingsScreenState> = MutableStateFlow(SettingsScreenState())
    val state : StateFlow<SettingsScreenState> = _state

    init {
        viewModelScope.launch {
            dataStoreRepository.settings.collect {
                settings -> _state.value = _state.value.copy(appSettings = settings ?: AppSettings())
            }
        }
    }

//    fun openDialog(){
//        _state.value = _state.value.copy(
//            isDialogOpen = true
//        )
//    }
//
//    fun closeDialog(){
//        _state.value = _state.value.copy(
//            isDialogOpen = false
//        )
//    }

    fun saveDisableAttemptWord(newVal : String) {
        _state.value = _state.value.copy(
            appSettings = _state.value.appSettings.copy(
                disableAttemptWord = newVal
            )
        )
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            dataStoreRepository.setSettings(_state.value.appSettings)
        }
    }


}