package com.example.lockedindungeon.viewmodels

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.utils.hash
import com.example.lockedindungeon.utils.parseNdefIntent
import com.example.lockedindungeon.utils.writeToTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DialogState {
    INPUT_PASSWORD,
    WRITE_TAG,
}

data class HomeState (
    public val message : String = "Test Home State",
    public val isBlockActive : Boolean = false,
    public val nfcHashedPassword : String = "",
    public val currentDayStreak : Int = 0,
    public val isNfcDialogOpen : Boolean = false,
    public val isWritePasswordDialogOpen : Boolean = false,

    // NfcDialog: read the card and toggle blocking
    public val nfcPasswordBuffer : String = "",
    public val nfcMessage : String? = null,

    // WriteTagPasswordDialog: register a new card
    public val dialogState : DialogState = DialogState.INPUT_PASSWORD,
    public val writeBuffer : String? = null,
    public val isTagDetected : Boolean = false,
    public val writeTagMessage : String? = null,
    public val writeTagErrorMessage : String? = null,
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
     * Entry point for a tapped card. While the write tag dialog is waiting for a card the
     * hashed password gets written into it, otherwise the card is read and the user is asked
     * for the password on it.
     */
    public fun onNdefIntent(intent : Intent){
        viewModelScope.launch {
            if(state.value.isWritePasswordDialogOpen &&
                state.value.dialogState == DialogState.WRITE_TAG){
                onTagDetected(intent)
            } else {
                val content = parseNdefIntent(intent)
                val hashedPw = content?.get(0)?.joinToString(", ") ?: ""

                _state.value = _state.value.copy(
                    nfcHashedPassword = hashedPw,
                    nfcPasswordBuffer = "",
                    nfcMessage = null,
                    isNfcDialogOpen = true
                )
            }
        }
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

    // ------------------------------------------------- WriteTagPasswordDialog

    public fun setIsWritePasswordDialogOpen(bool: Boolean) {
        _state.value = _state.value.copy(
            isWritePasswordDialogOpen = bool,
            dialogState = DialogState.INPUT_PASSWORD,
            writeBuffer = null,
            isTagDetected = false,
            writeTagMessage = null,
            writeTagErrorMessage = null
        )
    }

    public fun onWriteBufferChanged(newStr : String){
        _state.value = _state.value.copy(
            writeBuffer = newStr
        )
    }

    public fun onWritePasswordSubmit(){
        _state.value = _state.value.copy(
            dialogState = DialogState.WRITE_TAG
        )
    }

    private fun onTagDetected(intent : Intent) {
        val writeBuffer = _state.value.writeBuffer
        if(writeBuffer == null){
            _state.value = _state.value.copy(
                writeTagErrorMessage = "Please submit your password before tapping your card"
            )
            return
        }

        var message : String = ""
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if(tag == null){
            message = "Failed to detect tag, please try again"
        }
        else {
            writeToTag(tag, hash(writeBuffer))
            message = "data successfully written to tag"
        }

        _state.value = _state.value.copy(
            isTagDetected = true,
            writeTagMessage = message
        )
    }
}
