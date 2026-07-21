package com.example.lockedindungeon.viewmodels

import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.lockedindungeon.data.local.repositories.AppStateRepository
import com.example.lockedindungeon.utils.hash
import com.example.lockedindungeon.utils.parseNdefIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


data class NfcScanState(
    public val message : String? = null,
    public val passwordBuffer : String = "",
    public val cardPassword : String? = null
)

@HiltViewModel
class NfcScanViewmodel @Inject constructor(
    private val stateRepo: AppStateRepository
) : ViewModel() {

    private val _state : MutableState<NfcScanState> = mutableStateOf(NfcScanState())
    public val state : State<NfcScanState> = _state

    fun onWriteBufferChanged(newStr : String){
        _state.value = _state.value.copy(
            passwordBuffer = newStr
        )
    }

    fun setCardPassword(password: String) {
        _state.value = _state.value.copy(
            cardPassword = password
        )
    }

    fun onPasswordSubmit(onSuccess: () -> Unit){
        var message = ""
        if(_state.value.passwordBuffer.isEmpty()){
            message = "Password is empty"
        }else if(hash(_state.value.passwordBuffer) != _state.value.cardPassword){
            message = "Password does not match"
        }else{
            disableBlocking()
            onSuccess()
            return
        }

        _state.value = _state.value.copy(
            message = message
        )
    }

    fun reset() {
        _state.value = NfcScanState()
    }

    fun disableBlocking(){
        CoroutineScope(Dispatchers.Default).launch {
            stateRepo.setActive(false)
        }
    }
}