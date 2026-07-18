package com.example.lockedindungeon.viewmodels

import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

data class HomeState (
    public val message : String
)

class HomeViewModel constructor() : ViewModel() {
    private val _homeState: MutableState<HomeState> = mutableStateOf(HomeState("Test Home State"))
    public val homeState : State<HomeState> = _homeState

    public fun changeMessage(message : String){
        _homeState.value = _homeState.value.copy(
            message = message
        )
    }
}