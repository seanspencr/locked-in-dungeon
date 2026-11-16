package com.example.lockedindungeon.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.remote.UserRepository
import com.example.lockedindungeon.dto.Follow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FriendScreenTabState {
    FRIEND,
    FOLLOWER,
    FOLLOWING,
}

data class FriendScreenState(
    val tabState : FriendScreenTabState = FriendScreenTabState.FRIEND,
    val querySearch : String = "",
    val isSearchDialogOpen : Boolean = false,

    val followerList : List<Follow> = listOf(),
    val followingList : List<Follow> = listOf(),
    val friendList : List<Follow> = listOf()
)

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {
    private val _state : MutableStateFlow<FriendScreenState> = MutableStateFlow(FriendScreenState())
    val state : StateFlow<FriendScreenState> = _state

    private var searchJob: Job? = null

    fun changeTab(tabState: FriendScreenTabState) {
        _state.value = _state.value.copy(tabState = tabState)
    }

    fun openSearchDialog() {
        _state.value = _state.value.copy(isSearchDialogOpen = true, querySearch = "")
    }

    fun dismissSearchDialog() {
        searchJob?.cancel()
        _state.value = _state.value.copy(isSearchDialogOpen = false, querySearch = "")
    }

    fun onQuerySearchChange(query: String) {
        _state.value = _state.value.copy(querySearch = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            if (query.isNotBlank()) {
                runCatching {
                    userRepository.findUserByUsername(query)
                }.onFailure { err ->
                    Log.d("FriendViewModel", "findUserByUsername called: ${err.message}")
                }
            }
        }
    }
}
