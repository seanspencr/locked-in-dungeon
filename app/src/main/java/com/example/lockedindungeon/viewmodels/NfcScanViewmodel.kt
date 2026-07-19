package com.example.lockedindungeon.viewmodels

import android.R.id.message
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.lockedindungeon.utils.hash
import com.example.lockedindungeon.utils.parseNdefIntent
import com.example.lockedindungeon.utils.writeToTag
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


enum class NfcScanDialogState{
    READ_CARD,
    CHECK_PASSWORD
}

data class NfcScanState(
    public val isDetected : Boolean = false,
    public  val message : String? = null,
    public val passwordBuffer : String = "",
    public val cardPassword : String? = null,
    public val dialogState : NfcScanDialogState = NfcScanDialogState.READ_CARD
)

@HiltViewModel
class NfcScanViewmodel @Inject constructor() : ViewModel() {

    private val _state : MutableState<NfcScanState> = mutableStateOf(NfcScanState())
    public val state : State<NfcScanState> = _state

    fun onWriteBufferChanged(newStr : String){
        _state.value = _state.value.copy(
            passwordBuffer = newStr
        )
    }

    fun onPasswordSubmit(){
        var message = ""
        if(_state.value.passwordBuffer.isEmpty()){
            message = "Password is empty"
        }else if(hash(_state.value.passwordBuffer) != _state.value.cardPassword){
            message = "Password does not match"
        }else{
            message = "Password matched, blocking has been disabled"
        }

        _state.value = _state.value.copy(
            message = message
        )
    }

    fun reset() {
        _state.value = NfcScanState()
    }

    fun onTagDetected(intent : Intent) {
        var message = ""

        val content = parseNdefIntent(intent)
        if(content == null){
            message = "Card was detected but null"
            _state.value = _state.value.copy(
                isDetected = true,
                message = message
            )

        }else{
            val hashedPw = content[0]?.joinToString(", ") ?: "Index 0 is null"
            _state.value = _state.value.copy(
                cardPassword = hashedPw,
                dialogState = NfcScanDialogState.CHECK_PASSWORD
            )
        }


    }


}