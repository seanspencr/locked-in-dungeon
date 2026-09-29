package com.example.lockedindungeon.viewmodels

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.lockedindungeon.utils.hash
import com.example.lockedindungeon.utils.parseNdefIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


enum class NfcUnlockDialogStateEnum {
    INPUT_PASSWORD,
    INPUT_DISABLE_ATTEMPT_WORD
}
data class NfcUnlockDialogState(
    public val nfcMessage : String? = null,
    public val state : NfcUnlockDialogStateEnum = NfcUnlockDialogStateEnum.INPUT_PASSWORD,
    public val nfcHashedPassword : String? = null
)

@HiltViewModel
class NfcUnlockDialogViewmodel @Inject constructor() : ViewModel() {
    private val _state : MutableStateFlow<NfcUnlockDialogState> = MutableStateFlow(NfcUnlockDialogState())
    public val state : StateFlow<NfcUnlockDialogState> = _state

    private val tag : String = "NfcUnlockDialogViewmodel"
    fun reset(){
        _state.value = NfcUnlockDialogState()
    }

    fun onNdefIntent(intent: Intent){
        val content = parseNdefIntent(intent)
        Log.d(tag, "Content : $content")
        val hashedPw = content?.get(0)?.joinToString(", ") ?: ""
        Log.d(tag, "Hashed PW : $hashedPw")

        _state.value = _state.value.copy(
            nfcHashedPassword = hashedPw,
            nfcMessage = null,
        )
    }

    public fun submitPassword(inputPassword : String, onSuccess : () -> Unit){
        val message = when {
            inputPassword.isEmpty() -> "Password is empty"
            hash(inputPassword) != _state.value.nfcHashedPassword -> "Password does not match ${hash(inputPassword)} and ${_state.value.nfcHashedPassword}"
            else -> {
                onSuccess()
                return
            }
        }

        _state.value = _state.value.copy(
            nfcMessage = message
        )
    }
}