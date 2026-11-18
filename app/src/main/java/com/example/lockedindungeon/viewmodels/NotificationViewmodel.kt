package com.example.lockedindungeon.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.entities.Notification
import com.example.lockedindungeon.data.local.repositories.NotificationLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository : NotificationLocalRepository
) : ViewModel() {

    /** Everything FCM stored, newest first. */
    val notifications : StateFlow<List<Notification>> = notificationRepository.notifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun markAllAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.markAllAsRead()
        }
    }
}
