package com.example.lockedindungeon.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.data.remote.AuthRepository
import com.example.lockedindungeon.dto.MeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState (
    public val message : String = "Test Home State",
    public val isBlockActive : Boolean = false,
    public val currentDayStreak : Int = 0,
    public val isNfcDialogOpen : Boolean = false,
    public val isNotificationDialogDismissed : Boolean = false

    // NfcDialog: read the card and toggle blocking

)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stateRepository: AppDatastoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state: MutableStateFlow<HomeState> = MutableStateFlow(HomeState())
    public val state : StateFlow<HomeState> = _state


    init {
        viewModelScope.launch {
//            collect = observable.onChange = ()->{}
            stateRepository.isActive.collect {
                    it -> _state.value = _state.value.copy(
                        isBlockActive = it
                    )
            }
        }

        viewModelScope.launch {
            stateRepository.currentDayStreak.collect {
                it -> _state.value = _state.value.copy(
                    currentDayStreak = it
                )
            }
        }

        hydrateUser()
    }

    /**
     * Entry point for a card that is not being written by the register-a-card flow: the card
     * gets read and the user is asked for the password on it.
     */
    public fun onNdefIntent(intent : Intent){
//        klo ada ndef intent brarti dia mau unlock
        _state.value = _state.value.copy(
            isNfcDialogOpen = true
        )
    }

    public fun toggleBlockActive(){
        viewModelScope.launch {
            stateRepository.setActive(!_state.value.isBlockActive)
        }
    }

    // ---------------------------------------------------------------- NfcDialog


    /**
     * Checks the entered password against the one stored on the card. On a match the
     * dialog closes and blocking gets toggled, otherwise the reason is shown in the dialog.
     */
    public fun dismissNfcDialog(){
        _state.value = _state.value.copy(
            isNfcDialogOpen = false
        )
    }

    // -------------------------------------------------------- NotificationDialog

    /** Hides the "turn on notifications" prompt after the user answered it. */
    public fun dismissNotificationDialog(){
        _state.value = _state.value.copy(
            isNotificationDialogDismissed = true
        )
    }


    public fun hydrateUser(){
        viewModelScope.launch(Dispatchers.IO) {
            val userInfo = authRepository.tryGetUserInfo();
            when(userInfo) {
                is ApiResult.Error -> setMessage("Failed to fetch user info")
                is ApiResult.Success<MeResponse> -> {
                    setMessage("Hello ${userInfo.component2().username}")
                }
            }
        }
    }

    public fun setMessage(message : String) {
        _state.value = _state.value.copy(
            message = message
        )
    }
}
