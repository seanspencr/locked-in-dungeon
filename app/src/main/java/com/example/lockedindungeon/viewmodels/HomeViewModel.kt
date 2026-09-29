package com.example.lockedindungeon.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.utils.hash
import com.example.lockedindungeon.utils.parseNdefIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState (
    public val message : String = "Test Home State",
    public val isBlockActive : Boolean = false,
    public val nfcHashedPassword : String = "",
    public val currentDayStreak : Int = 0,
    public val isNfcDialogOpen : Boolean = false,

    // NfcDialog: read the card and toggle blocking
    public val nfcPasswordBuffer : String = "",
    public val nfcMessage : String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stateRepository: AppDatastoreRepository
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
    }

    /**
     * Entry point for a card that is not being written by the register-a-card flow: the card
     * gets read and the user is asked for the password on it.
     */
    public fun onNdefIntent(intent : Intent){
        val content = parseNdefIntent(intent)
        val hashedPw = content?.get(0)?.joinToString(", ") ?: ""

        _state.value = _state.value.copy(
            nfcHashedPassword = hashedPw,
            nfcPasswordBuffer = "",
            nfcMessage = null,
            isNfcDialogOpen = true
        )
    }

    public fun toggleBlockActive(){
        viewModelScope.launch {
            stateRepository.setActive(!_state.value.isBlockActive)
        }
    }

    // ---------------------------------------------------------------- NfcDialog

    public fun onNfcPasswordChanged(newStr : String){
        _state.value = _state.value.copy(
            nfcPasswordBuffer = newStr
        )
    }

    /**
     * Checks the entered password against the one stored on the card. On a match the
     * dialog closes and blocking gets toggled, otherwise the reason is shown in the dialog.
     */
    public fun onNfcPasswordSubmit(){
        val password = _state.value.nfcPasswordBuffer
        val message = when {
            password.isEmpty() -> "Password is empty"
            hash(password) != _state.value.nfcHashedPassword -> "Password does not match"
            else -> {
                toggleBlockActive()
                dismissNfcDialog()
                return
            }
        }

        _state.value = _state.value.copy(
            nfcMessage = message
        )
    }

    public fun dismissNfcDialog(){
        _state.value = _state.value.copy(
            isNfcDialogOpen = false,
            nfcPasswordBuffer = "",
            nfcMessage = null
        )
    }
}
