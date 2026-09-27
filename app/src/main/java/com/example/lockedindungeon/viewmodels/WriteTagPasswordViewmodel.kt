package com.example.lockedindungeon.viewmodels

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.lockedindungeon.utils.hash
import com.example.lockedindungeon.utils.writeToTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


enum class DialogState{
    INPUT_PASSWORD,
    WRITE_TAG,
}

data class WriteTagPasswordState(
    val dialogState : DialogState = DialogState.INPUT_PASSWORD,
    val writeBuffer :  String? = null,
    val isDetected : Boolean = false,
    val message : String? = null,
    val errorMessage : String? = null
)

@HiltViewModel
class WriteTagPasswordViewmodel @Inject constructor() : ViewModel() {
    private val _state : MutableStateFlow<WriteTagPasswordState> = MutableStateFlow(WriteTagPasswordState())
    public val state : StateFlow<WriteTagPasswordState> = _state

    fun onWriteBufferChanged(newStr : String){
        _state.value = _state.value.copy(
            writeBuffer = newStr
        )
    }

    fun onPasswordSubmit(){
        _state.value = _state.value.copy(
            dialogState = DialogState.WRITE_TAG
        )
    }

    fun reset(){
        _state.value = WriteTagPasswordState()
    }

    fun onTagDetected(intent : Intent) {
        var message : String = ""
        when(_state.value.dialogState){
            DialogState.INPUT_PASSWORD -> _state.value = _state.value.copy(
                errorMessage = "Please submit your password before tapping your card"
            )
            DialogState.WRITE_TAG -> {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                if(tag == null){
                    message = "Failed to detect tag, please try again"
                }
                else {
                    writeToTag(tag, hash(_state.value.writeBuffer  ?: throw IllegalStateException("Tried to write an empty string to tag")));
                    message = "data successfully written to tag"
                }
            }
        }

        _state.value = _state.value.copy(
            isDetected = true,
            message = message
        )
    }
}