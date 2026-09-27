package com.example.lockedindungeon.viewmodels

import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.data.local.repositories.AppStateRepository
import com.example.lockedindungeon.utils.parseNdefIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState (
    public val message : String,
    public val isBlockActive : Boolean = false,
    public val nfcHashedPassword : String = "",
    public val currentDayStreak : Int = 0,
    public val isNfcDialogOpen : Boolean = false,
    public val isWritePasswordDialogOpen : Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stateRepository: AppStateRepository
) : ViewModel() {

    private val _state: MutableStateFlow<HomeState> = MutableStateFlow(
        HomeState(
            message = "Test Home State"
        )
    )
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

    public fun onNdefIntent(intent : Intent){
        viewModelScope.launch {
            if(state.value.isBlockActive){
                val content = parseNdefIntent(intent)
                val hashedPw = content?.get(0)?.joinToString(", ") ?: ""

                _state.value = _state.value.copy(
                    nfcHashedPassword = hashedPw
                )
            }else {
    //            trigger enable
                _state.value = _state.value.copy(
                    message = "Blocking enabled"
                )
                stateRepository.setActive(true)
            }
        }
    }


    
    public fun toggleBlockActive(){
        viewModelScope.launch {
            stateRepository.setActive(!_state.value.isBlockActive)
        }
    }

    fun setIsNfcDialogOpen(bool: Boolean) {
        _state.value = _state.value.copy(
            isNfcDialogOpen = bool
        )
    }
    fun setIsWritePasswordDialogOpen(bool: Boolean) {
        _state.value = _state.value.copy(
            isWritePasswordDialogOpen = bool
        )
    }

}