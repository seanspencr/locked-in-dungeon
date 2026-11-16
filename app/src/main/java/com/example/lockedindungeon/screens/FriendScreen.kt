package com.example.lockedindungeon.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lockedindungeon.components.TitleBar
import com.example.lockedindungeon.data.model.SearchedUser
import com.example.lockedindungeon.dto.Follow
import com.example.lockedindungeon.viewmodels.FriendScreenTabState
import com.example.lockedindungeon.viewmodels.FriendViewModel

@Composable
fun FriendScreen(
    viewModel: FriendViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TitleBar(text = "Friends")

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                FriendScreenTabState.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = index == state.tabState.ordinal,
                        onClick = { viewModel.changeTab(tab) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = FriendScreenTabState.entries.size
                        )
                    ) {
                        Text(
                            text = when (tab) {
                                FriendScreenTabState.FRIEND -> "Friends"
                                FriendScreenTabState.FOLLOWER -> "Followers"
                                FriendScreenTabState.FOLLOWING -> "Following"
                            }
                        )
                    }
                }
            }

            val currentList = when (state.tabState) {
                FriendScreenTabState.FRIEND -> state.friendList
                FriendScreenTabState.FOLLOWER -> state.followerList
                FriendScreenTabState.FOLLOWING -> state.followingList
            }

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${state.tabState.name.lowercase()}s found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentList) { follow ->
                        FriendCard(follow = follow)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.openSearchDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Search & Add Friend"
            )
        }
    }

    if (state.isSearchDialogOpen) {
        SearchUserDialog(
            query = state.querySearch,
            searchResultUsers = state.searchResultUsers,
            isSearching = state.isSearching,
            pendingFollowUserIds = state.pendingFollowUserIds,
            onQueryChange = { viewModel.onQuerySearchChange(it) },
            onFollow = { viewModel.follow(it) },
            onUnfollow = { viewModel.unfollow(it) },
            onDismissRequest = { viewModel.dismissSearchDialog() }
        )
    }
}

@Composable
fun SearchUserDialog(
    query: String,
    searchResultUsers: List<SearchedUser>,
    isSearching: Boolean,
    pendingFollowUserIds: Set<Long>,
    onQueryChange: (String) -> Unit,
    onFollow: (SearchedUser) -> Unit,
    onUnfollow: (SearchedUser) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Search User",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Search by username") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSearching) {
                        CircularProgressIndicator()
                    } else if (query.isNotBlank() && searchResultUsers.isEmpty()) {
                        Text(
                            text = "No users found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(searchResultUsers) { user ->
                                UserCard(
                                    user = user,
                                    isActionPending = user.id != null && user.id in pendingFollowUserIds,
                                    onFollow = { onFollow(user) },
                                    onUnfollow = { onUnfollow(user) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: SearchedUser,
    isActionPending: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Icon",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (user.isFollowed) {
            OutlinedButton(onClick = onUnfollow, enabled = !isActionPending) {
                Text("Unfollow")
            }
        } else {
            Button(onClick = onFollow, enabled = !isActionPending) {
                Text("Follow")
            }
        }
    }
}

@Composable
fun FriendCard(follow: Follow) {
    val username = follow.follower?.username ?: follow.target?.username ?: "Unknown User"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Icon",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
