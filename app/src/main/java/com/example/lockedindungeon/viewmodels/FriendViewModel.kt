package com.example.lockedindungeon.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.data.model.SearchedUser
import com.example.lockedindungeon.data.remote.FriendRepository
import com.example.lockedindungeon.dto.Follow
import com.example.lockedindungeon.dto.FollowUserRequest
import com.example.lockedindungeon.dto.FriendListResponse
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
    val searchResultUsers : List<SearchedUser> = listOf(),
    val isSearching : Boolean = false,

    val followerList : List<Follow> = listOf(),
    /** Users the client currently treats as followed, updated optimistically on follow/unfollow. */
    val followingUserList : List<SearchedUser> = listOf(),
    val friendList : List<Follow> = listOf()
)

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val friendRepository: FriendRepository
): ViewModel() {

    companion object {
        private const val TAG = "FriendViewModel"

        /** The following endpoint is paginated, one page is enough to resolve the follow state. */
        private const val FOLLOWING_PAGE_SIZE = 100
    }

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
        // refreshes the follow state, so rows already followed show Unfollow right away
        refreshFollowingUsers()
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
                        Log.d(TAG, "getFriendSuggestions error: ${result.message}")
                        _state.value = _state.value.copy(searchResultUsers = emptyList(), isSearching = false)
                    }
                    is ApiResult.Success<FriendListResponse?> -> {
                        val following = _state.value.followingUserList
                        val users = result.data?.paginatedUser.orEmpty().map { user ->
                            SearchedUser(
                                id = user.id,
                                username = user.username.orEmpty(),
                                isFollowed = following.any { it.id == user.id }
                            )
                        }
                        _state.value = _state.value.copy(searchResultUsers = users, isSearching = false)
                    }
                }
            }.onFailure { err ->
                Log.d(TAG, "getFriendSuggestions failed: ${err.message}")
                _state.value = _state.value.copy(searchResultUsers = emptyList(), isSearching = false)
            }
        }
    }

    fun follow(user : SearchedUser) {
        applyFollow(user, isFollowed = true)
        sendFollowChange(user, isFollowed = true)
    }

    fun unfollow(user : SearchedUser) {
        applyFollow(user, isFollowed = false)
        sendFollowChange(user, isFollowed = false)
    }

    /** Optimistic: the search row and [FriendScreenState.followingUserList] change before the call. */
    private fun applyFollow(user : SearchedUser, isFollowed : Boolean) {
        val userId = user.id ?: return

        _state.value = _state.value.copy(
            followingUserList = if (isFollowed) {
                _state.value.followingUserList.filterNot { it.id == userId } + user.copy(isFollowed = true)
            } else {
                _state.value.followingUserList.filterNot { it.id == userId }
            },
            searchResultUsers = _state.value.searchResultUsers.map {
                if (it.id == userId) it.copy(isFollowed = isFollowed) else it
            }
        )
    }

    private fun sendFollowChange(user : SearchedUser, isFollowed : Boolean) {
        val userId = user.id ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val request = FollowUserRequest(userId = userId)
            val result : ApiResult<*> = if (isFollowed) {
                friendRepository.followUser(request)
            } else {
                friendRepository.unfollowUser(request)
            }

            (result as? ApiResult.Error)?.let { error ->
                Log.d(TAG, "follow=$isFollowed for $userId failed, reverting: ${error.message}")
                applyFollow(user, isFollowed = !isFollowed)
            }
        }
    }

    private fun refreshFollowingUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = friendRepository.getFollowing(page = 0, size = FOLLOWING_PAGE_SIZE)) {
                is ApiResult.Success<FriendListResponse> -> {
                    val following = result.data.paginatedUser.orEmpty().map { user ->
                        SearchedUser(
                            id = user.id,
                            username = user.username.orEmpty(),
                            isFollowed = true
                        )
                    }
                    _state.value = _state.value.copy(
                        followingUserList = following,
                        searchResultUsers = _state.value.searchResultUsers.map { row ->
                            row.copy(isFollowed = following.any { it.id == row.id })
                        }
                    )
                }
                is ApiResult.Error -> Log.d(TAG, "getFollowing error: ${result.message}")
            }
        }
    }
}
