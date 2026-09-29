package com.example.lockedindungeon.viewmodels

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.utils.hash
import com.example.lockedindungeon.utils.writeToTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WriteTagDialogState {
    INPUT_PASSWORD,
    WRITE_TAG,
}

data class WriteTagState(
    val writeTagDialogState : WriteTagDialogState = WriteTagDialogState.INPUT_PASSWORD,
    val writeBuffer : String? = null,
    val isTagDetected : Boolean = false,
    val message : String? = null,
    val errorMessage : String? = null,
)

/**
 * State of the register-a-card flow shown by [com.example.lockedindungeon.components.WriteTagPasswordDialog].
 * Used from both the home and the settings screen, so a single instance is hoisted in
 * NavigationParent and MainActivity hands tapped cards to it.
 */
@HiltViewModel
class WriteTagViewModel @Inject constructor() : ViewModel() {

    private val _state : MutableStateFlow<WriteTagState> = MutableStateFlow(WriteTagState())
    val state : StateFlow<WriteTagState> = _state

    /** Clears the flow, called when the dialog gets opened or dismissed. */
    fun reset() {
        _state.value = WriteTagState()
    }

    fun onPasswordChanged(newStr : String) {
        _state.value = _state.value.copy(writeBuffer = newStr)
    }

    fun onPasswordSubmit() {
        _state.value = _state.value.copy(writeTagDialogState = WriteTagDialogState.WRITE_TAG)
    }

    /**
     * Hands a tapped card to the dialog while the user is waiting on the write step. Returns true
     * when the card was written to, false when no card is being registered right now, so the caller
     * can route the card somewhere else.
     */
    fun onNdefIntent(intent : Intent) : Boolean {
        val state = _state.value
        if(state.writeTagDialogState != WriteTagDialogState.WRITE_TAG) return false

        viewModelScope.launch {
            writeTag(intent)
        }
        return true
    }

    private fun writeTag(intent : Intent) {
        val writeBuffer = _state.value.writeBuffer
        if(writeBuffer == null){
            _state.value = _state.value.copy(
                errorMessage = "Please submit your password before tapping your card"
            )
            return
        }

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        val message = when {
            tag == null -> "Failed to detect tag, please try again"
            writeToTag(tag, hash(writeBuffer)) -> "Data successfully written to tag"
            else -> "Failed to write to tag, please try again"
        }

        _state.value = _state.value.copy(
            isTagDetected = true,
            message = message
        )
    }
}
