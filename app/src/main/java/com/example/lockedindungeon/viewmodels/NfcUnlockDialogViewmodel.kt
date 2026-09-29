package com.example.lockedindungeon.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.utils.hash
import com.example.lockedindungeon.utils.parseNdefIntent
import com.example.lockedindungeon.workers.SnoozeWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject


enum class NfcUnlockDialogStateEnum {
    INPUT_PASSWORD,
    INPUT_DISABLE_ATTEMPT_WORD
}
data class NfcUnlockDialogState(
    public val passwordInputMessage : String? = null,
    public val state : NfcUnlockDialogStateEnum = NfcUnlockDialogStateEnum.INPUT_DISABLE_ATTEMPT_WORD,
    public val nfcHashedPassword : String? = null,
    public val disableAttemptMessage : String? = null,
    public val disableAttemptWord : String = ""
)

@HiltViewModel
class NfcUnlockDialogViewmodel @Inject constructor(
    @ApplicationContext  private val appContext : Context,
    private val datastoreRepository: AppDatastoreRepository
) : ViewModel() {

    private lateinit var _state : MutableStateFlow<NfcUnlockDialogState>
    public lateinit var state : StateFlow<NfcUnlockDialogState>

    private val tag : String = "NfcUnlockDialogViewmodel"

    companion object {
        const val SNOOZE_WORK_NAME = "reactivate_blocking_after_snooze"
    }
    fun reset(){
        _state.value = NfcUnlockDialogState()
        viewModelScope.launch {
            val setting = datastoreRepository.settings.first()
            _state.value = _state.value.copy(
                disableAttemptWord = setting.disableAttemptWord
            )
        }
    }

    init {
        viewModelScope.launch {


            _state = MutableStateFlow(NfcUnlockDialogState())
            state = _state

            datastoreRepository.settings.collect {
                setting -> _state.value = _state.value.copy(disableAttemptWord = setting.disableAttemptWord)
            }
        }

    }

    fun onNdefIntent(intent: Intent){
        val content = parseNdefIntent(intent)
        Log.d(tag, "Content : $content")
        val hashedPw = content?.get(0)?.joinToString(", ") ?: ""
        Log.d(tag, "Hashed PW : $hashedPw")

        _state.value = _state.value.copy(
            nfcHashedPassword = hashedPw,
            passwordInputMessage = null,
        )

        viewModelScope.launch {
            if(!datastoreRepository.isActive.first()){
                _state.value = _state.value.copy(state = NfcUnlockDialogStateEnum.INPUT_PASSWORD    )
            }
        }

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
            passwordInputMessage = message
        )
    }

    public fun scheduleSnooze(durationMinute : Int = 5){
        val request : OneTimeWorkRequest = OneTimeWorkRequestBuilder<SnoozeWorker>()
            .setInitialDelay(Duration.ofMinutes(durationMinute.toLong()))
            .build()

        // REPLACE: snoozing again must not leave the older timer armed, it would re-enable
        // blocking before the interval the user just picked is over
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            SNOOZE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    public fun submitDisableAttemptWord(inputAttempt : String, onSuccess: () -> Unit){
        if (inputAttempt != _state.value.disableAttemptWord){
            _state.value = _state.value.copy(
                disableAttemptMessage = "Make sure to type correctly, input :  $inputAttempt, riyal : ${_state.value.disableAttemptWord}"
            )
            return
        }
        onSuccess()
    }

    fun continueState(onFinalState : ()-> Unit) {
        when(_state.value.state){
            NfcUnlockDialogStateEnum.INPUT_PASSWORD ->  onFinalState.invoke()
            NfcUnlockDialogStateEnum.INPUT_DISABLE_ATTEMPT_WORD -> {
                Log.d(tag, "State changed from ${_state.value.state}")
                _state.value = _state.value.copy(state = NfcUnlockDialogStateEnum.INPUT_PASSWORD)
                Log.d(tag, "State changed to ${_state.value.state}")
            }
        }
    }
}