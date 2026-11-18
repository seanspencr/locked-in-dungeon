package com.example.lockedindungeon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lockedindungeon.data.local.entities.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notification ORDER BY timestamp DESC")
    fun selectNotifications() : Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notification WHERE isRead = 0")
    fun selectUnreadCount() : Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification : Notification) : Long

    @Query("UPDATE notification SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notification")
    suspend fun deleteAll()
}
