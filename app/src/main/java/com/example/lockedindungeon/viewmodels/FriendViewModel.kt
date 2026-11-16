package com.example.lockedindungeon.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.data.model.SearchedUser
import com.example.lockedindungeon.data.remote.FriendRepository
import com.example.lockedindungeon.dto.FollowUserRequest
import com.example.lockedindungeon.dto.FriendListResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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
    /** True while the pull to refresh gesture is fetching the three lists. */
    val isRefreshing : Boolean = false,

    val followerList : List<SearchedUser> = listOf(),
    /** Users the client currently treats as followed, updated optimistically on follow/unfollow. */
    val followingUserList : List<SearchedUser> = listOf(),
    val friendList : List<SearchedUser> = listOf()
)

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val friendRepository: FriendRepository
): ViewModel() {

    companion object {
        private const val TAG = "FriendViewModel"

        /** The endpoints are paginated, one page is enough for the list screens. */
        private const val PAGE_SIZE = 100
    }

    private val _state : MutableStateFlow<FriendScreenState> = MutableStateFlow(FriendScreenState())
    val state : StateFlow<FriendScreenState> = _state

    private var searchJob: Job? = null
    private var refreshJob: Job? = null

    fun changeTab(tabState: FriendScreenTabState) {
        _state.value = _state.value.copy(tabState = tabState)
    }

    /**
     * Fetches friends, followers and following in parallel. Called from the screen's LaunchedEffect
     * on first composition and from the pull to refresh gesture.
     */
    fun refresh() {
        refreshJob?.cancel()
        _state.value = _state.value.copy(isRefreshing = true)

        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val followingDeferred = async { friendRepository.getFollowing(page = 0, size = PAGE_SIZE) }
                val followerDeferred = async { friendRepository.getFollowers(page = 0, size = PAGE_SIZE) }
                val friendDeferred = async { friendRepository.getFriends(page = 0, size = PAGE_SIZE) }

                val following = followingDeferred.await().usersOf(isFollowed = true)
                val followedIds = following.mapNotNull { it.id }.toSet()
                val followers = followerDeferred.await().usersOf().withFollowState(followedIds)
                val friends = friendDeferred.await().usersOf().withFollowState(followedIds)

                _state.value = _state.value.copy(
                    followingUserList = following,
                    followerList = followers,
                    friendList = friends,
                    searchResultUsers = _state.value.searchResultUsers.withFollowState(followedIds),
                    isRefreshing = false
                )
            }.onFailure { error ->
                Log.d(TAG, "refresh failed: ${error.message}")
                _state.value = _state.value.copy(isRefreshing = false)
            }
        }
    }

    fun openSearchDialog() {
        _state.value = _state.value.copy(
            isSearchDialogOpen = true,
            querySearch = "",
            searchResultUsers = emptyList(),
            isSearching = false
        )
        // refreshes the follow state, so rows already followed show Unfollow right away
        refresh()
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
                        val followedIds = _state.value.followingUserList.mapNotNull { it.id }.toSet()
                        val users = result.data?.paginatedUser.orEmpty().map { user ->
                            SearchedUser(
                                id = user.id,
                                username = user.username.orEmpty(),
                                isFollowed = user.id != null && user.id in followedIds
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

    /** Optimistic: the lists change before the request even leaves the device. */
    private fun applyFollow(user : SearchedUser, isFollowed : Boolean) {
        val userId = user.id ?: return

        _state.value = _state.value.copy(
            followingUserList = if (isFollowed) {
                _state.value.followingUserList.filterNot { it.id == userId } + user.copy(isFollowed = true)
            } else {
                _state.value.followingUserList.filterNot { it.id == userId }
            },
            followerList = _state.value.followerList.map {
                if (it.id == userId) it.copy(isFollowed = isFollowed) else it
            },
            friendList = _state.value.friendList.map {
                if (it.id == userId) it.copy(isFollowed = isFollowed) else it
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

    /** Maps a paginated user response to UI rows, logging instead of crashing on errors. */
    private fun ApiResult<FriendListResponse>.usersOf(isFollowed : Boolean = false) : List<SearchedUser> =
        when (this) {
            is ApiResult.Success<FriendListResponse> -> data.paginatedUser.orEmpty().map { user ->
                SearchedUser(
                    id = user.id,
                    username = user.username.orEmpty(),
                    isFollowed = isFollowed
                )
            }
            is ApiResult.Error -> {
                Log.d(TAG, "fetching users failed: $message")
                emptyList()
            }
        }

    private fun List<SearchedUser>.withFollowState(followedIds : Set<Long>) : List<SearchedUser> =
        map { user -> user.copy(isFollowed = user.id != null && user.id in followedIds) }
}
