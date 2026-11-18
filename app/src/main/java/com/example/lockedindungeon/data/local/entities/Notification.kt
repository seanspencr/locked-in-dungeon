package com.example.lockedindungeon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification")
data class Notification(

    @PrimaryKey(autoGenerate = true)
    val id : Long,
    val type: NotificationType = NotificationType.BRAG,           // e.g., "ORDER_SHIPPED", "CHAT_MESSAGE", "PROMO"
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val timestamp: Long,
    val payloadJson: String?
)

enum class NotificationType(){
    FOLLOW,
    BRAG
}
