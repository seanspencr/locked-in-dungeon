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
    /** Users with a follow/unfollow call in flight, their button is disabled meanwhile. */
    val pendingFollowUserIds : Set<Long> = emptySet(),

    val followerList : List<Follow> = listOf(),
    val followingList : List<Follow> = listOf(),
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

    /** Ids the signed in user already follows, used to pick between Follow and Unfollow. */
    private var followedUserIds : Set<Long> = emptySet()

    fun changeTab(tabState: FriendScreenTabState) {
        _state.value = _state.value.copy(tabState = tabState)
    }

    fun openSearchDialog() {
        _state.value = _state.value.copy(
            isSearchDialogOpen = true,
            querySearch = "",
            searchResultUsers = emptyList(),
            isSearching = false,
            pendingFollowUserIds = emptySet()
        )
        // refreshes the follow state, so rows already followed show Unfollow right away
        refreshFollowedUserIds()
    }

    fun dismissSearchDialog() {
        searchJob?.cancel()
        _state.value = _state.value.copy(
            isSearchDialogOpen = false,
            querySearch = "",
            searchResultUsers = emptyList(),
            isSearching = false,
            pendingFollowUserIds = emptySet()
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
                        val users = result.data?.paginatedUser.orEmpty().map { user ->
                            SearchedUser(
                                id = user.id,
                                username = user.username.orEmpty(),
                                isFollowed = user.id != null && user.id in followedUserIds
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
        updateFollow(user = user, follow = true)
    }

    fun unfollow(user : SearchedUser) {
        updateFollow(user = user, follow = false)
    }

    private fun refreshFollowedUserIds() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = friendRepository.getFollowing(page = 0, size = FOLLOWING_PAGE_SIZE)) {
                is ApiResult.Success<FriendListResponse> -> {
                    followedUserIds = result.data.paginatedUser.orEmpty().mapNotNull { it.id }.toSet()
                    // re-apply the fresh state to whatever is already on screen
                    _state.value = _state.value.copy(
                        searchResultUsers = _state.value.searchResultUsers.map { user ->
                            user.copy(isFollowed = user.id != null && user.id in followedUserIds)
                        }
                    )
                }
                is ApiResult.Error -> Log.d(TAG, "getFollowing error: ${result.message}")
            }
        }
    }

    private fun updateFollow(user : SearchedUser, follow : Boolean) {
        val userId = user.id ?: return
        if (userId in _state.value.pendingFollowUserIds) return

        _state.value = _state.value.copy(
            pendingFollowUserIds = _state.value.pendingFollowUserIds + userId
        )

        viewModelScope.launch(Dispatchers.IO) {
            val request = FollowUserRequest(userId = userId)
            val result : ApiResult<*> = if (follow) {
                friendRepository.followUser(request)
            } else {
                friendRepository.unfollowUser(request)
            }
            val error : String? = (result as? ApiResult.Error)?.message

            if (error == null) {
                followedUserIds = if (follow) followedUserIds + userId else followedUserIds - userId
                _state.value = _state.value.copy(
                    pendingFollowUserIds = _state.value.pendingFollowUserIds - userId,
                    searchResultUsers = _state.value.searchResultUsers.map {
                        if (it.id == userId) it.copy(isFollowed = follow) else it
                    }
                )
            } else {
                Log.d(TAG, "follow=$follow for $userId failed: $error")
                _state.value = _state.value.copy(
                    pendingFollowUserIds = _state.value.pendingFollowUserIds - userId
                )
            }
        }
    }
}
