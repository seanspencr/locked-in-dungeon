package com.example.lockedindungeon.viewmodels

import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.repositories.AppStateRepository
import com.example.lockedindungeon.utils.parseNdefIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState (
    public val message : String,
    public val isBlockActive : Boolean = false,
    public val isPackageListDialogOpen : Boolean = false,
    public val isWritePasswordDialogOpen : Boolean = false,
    public val isNfcDialogOpen : Boolean = false,
    public val nfcHashedPassword : String = "",
    public val isBlockingDetailDialogOpen: Boolean = false,
    public val selectedPackageName: String = "",
    public val selectedDisplayName: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stateRepository: AppStateRepository
) : ViewModel() {

    private val _state: MutableState<HomeState> = mutableStateOf(
        HomeState(
            message = "Test Home State"
        )
    )
    public val state : State<HomeState> = _state


    init {
        viewModelScope.launch {
//            collect = observable.onChange = ()->{}
            stateRepository.isActive.collect {
                    it -> _state.value = _state.value.copy(
                        isBlockActive = it
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
                    isNfcDialogOpen = true,
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

    public fun changeMessage(message : String){
        _state.value = _state.value.copy(
            message = message
        )
    }

    public fun setPackageListOpenState(isOpen : Boolean){
        _state.value = _state.value.copy(
            isPackageListDialogOpen = isOpen
        )
    }
    
    public fun toggleBlockActive(){
        viewModelScope.launch {
            stateRepository.setActive(!_state.value.isBlockActive)
        }
    }

    fun setNfcDialogOpenState(isOpen: Boolean) {
        _state.value = _state.value.copy(
            isNfcDialogOpen = isOpen
        )
    }

    fun setWritePasswordDialogOpenState(isOpen: Boolean) {
        _state.value = _state.value.copy(
            isWritePasswordDialogOpen = isOpen
        )

    }

    fun openBlockingDetail(packageName: String, displayName: String) {
        _state.value = _state.value.copy(
            isBlockingDetailDialogOpen = true,
            selectedPackageName = packageName,
            selectedDisplayName = displayName,
            isPackageListDialogOpen = false
        )
    }

    fun closeBlockingDetail() {
        _state.value = _state.value.copy(
            isBlockingDetailDialogOpen = false
        )
    }
}