package com.example.lockedindungeon.data.local.repositories

import com.example.lockedindungeon.data.local.AppDatabase
import com.example.lockedindungeon.data.local.entities.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local storage for the notifications the app received. Written by
 * [com.example.lockedindungeon.services.FirebaseMessagingServiceImpl] and read by the notification
 * screen.
 */
@Singleton
class NotificationLocalRepository @Inject constructor(
    private val db : AppDatabase,
    private val applicationScope : CoroutineScope
) {

    private val notificationDao = db.notificationDao()

    val notifications : StateFlow<List<Notification>> = notificationDao
        .selectNotifications()
        .stateIn(scope = applicationScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    val unreadCount : StateFlow<Int> = notificationDao
        .selectUnreadCount()
        .stateIn(scope = applicationScope, started = SharingStarted.Eagerly, initialValue = 0)

    suspend fun save(notification : Notification) : Long = notificationDao.insert(notification)

    suspend fun markAllAsRead() = notificationDao.markAllAsRead()

    suspend fun clear() = notificationDao.deleteAll()
}
