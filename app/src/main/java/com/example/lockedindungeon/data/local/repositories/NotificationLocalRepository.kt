package com.example.lockedindungeon.data.local.repositories

import com.example.lockedindungeon.data.local.AppDatabase
import com.example.lockedindungeon.data.local.entities.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local storage for the notifications the app received. Written by
 * [com.example.lockedindungeon.services.FirebaseMessagingServiceImpl] and read by the notification
 * screen.
 */
@Singleton
class NotificationLocalRepository @Inject constructor(
    private val db : AppDatabase
) {

    private val notificationDao = db.notificationDao()

    val notifications : Flow<List<Notification>> = notificationDao.selectNotifications()

    val unreadCount : Flow<Int> = notificationDao.selectUnreadCount()

    suspend fun save(notification : Notification) : Long = notificationDao.insert(notification)

    suspend fun markAllAsRead() = notificationDao.markAllAsRead()

    suspend fun clear() = notificationDao.deleteAll()
}
