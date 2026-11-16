package com.example.lockedindungeon.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.data.remote.FriendRepository
import com.example.lockedindungeon.dto.Follow
import com.example.lockedindungeon.dto.FriendListResponse
import com.example.lockedindungeon.dto.User
import com.example.lockedindungeon.dto.UserDto
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
    val searchResultUsers : List<UserDto> = listOf(),
    val isSearching : Boolean = false,

    val followerList : List<Follow> = listOf(),
    val followingList : List<Follow> = listOf(),
    val friendList : List<Follow> = listOf()
)

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val friendRepository: FriendRepository
): ViewModel() {
    private val _state : MutableStateFlow<FriendScreenState> = MutableStateFlow(FriendScreenState())
    val state : StateFlow<FriendScreenState> = _state

    private var searchJob: Job? = null

    fun changeTab(tabState: FriendScreenTabState) {
        _state.value = _state.value.copy(tabState = tabState)
    }

    fun openSearchDialog() {
        _state.value = _state.value.copy(
            isSearchDialogOpen = true,
            querySearch = "",
            searchResultUsers = emptyList(),
            isSearching = false
        )
    }

    fun dismissSearchDialog() {
        searchJob?.cancel()
        _state.value = _state.value.copy(
            isSearchDialogOpen = false,
            querySearch = "",
            searchResultUsers = emptyList(),
            isSearching = false
        )
    }

    fun onQuerySearchChange(query: String) {
        _state.value = _state.value.copy(querySearch = query)
        searchJob?.cancel()

        if (query.isBlank()) {
            _state.value = _state.value.copy(searchResultUsers = emptyList(), isSearching = false)
            return
        }

        _state.value = _state.value.copy(isSearching = true)
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            runCatching {
                val result : ApiResult<FriendListResponse?> = friendRepository.getFriendSuggestions(query)
                when(result) {
                    is ApiResult.Error -> {
                        Log.d("FriendViewModel", "getFriendSuggestions error: ${result.message}")
                        _state.value = _state.value.copy(searchResultUsers = emptyList(), isSearching = false)
                    }
                    is ApiResult.Success<FriendListResponse?> -> {
                        val users = result.data?.component1() ?: emptyList()
                        _state.value = _state.value.copy(searchResultUsers = users, isSearching = false)
                    }
                }
            }.onFailure { err ->
                Log.d("FriendViewModel", "getFriendSuggestions failed: ${err.message}")
                _state.value = _state.value.copy(searchResultUsers = emptyList(), isSearching = false)
            }
        }
    }
}
