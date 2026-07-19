package com.example.lockedindungeon.viewmodels

import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.repositories.AppStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState (
    public val message : String,
    public val isBlockActive : Boolean = false,
    public val isPackageListDialogOpen : Boolean = false,
    public val isWritePasswordDialogOpen : Boolean = false,
    public val isNfcDialogOpen : Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stateRepository: AppStateRepository
) : ViewModel() {

    private val _homeState: MutableState<HomeState> = mutableStateOf(
        HomeState(
            message = "Test Home State"
        )
    )
    public val homeState : State<HomeState> = _homeState


    init {
        viewModelScope.launch {
//            collect = observable.onChange = ()->{}
            stateRepository.isActive.collect {
                    it -> _homeState.value = _homeState.value.copy(
                        isBlockActive = it
                    )
            }
        }
    }

    public fun changeMessage(message : String){
        _homeState.value = _homeState.value.copy(
            message = message
        )
    }

    public fun setPackageListOpenState(isOpen : Boolean){
        _homeState.value = _homeState.value.copy(
            isPackageListDialogOpen = isOpen
        )
    }
    
    public fun toggleBlockActive(){
        viewModelScope.launch {
            stateRepository.setActive(!_homeState.value.isBlockActive)
        }
    }

    fun setNfcDialogOpenState(isOpen: Boolean) {
        _homeState.value = _homeState.value.copy(
            isNfcDialogOpen = isOpen
        )
    }

    fun setWritePasswordDialogOpenState(isOpen: Boolean) {
        _homeState.value = _homeState.value.copy(
            isWritePasswordDialogOpen = isOpen
        )

    }
}