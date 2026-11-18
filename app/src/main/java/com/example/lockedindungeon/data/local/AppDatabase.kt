package com.example.lockedindungeon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lockedindungeon.data.local.dao.AppBlockingDetailDao
import com.example.lockedindungeon.data.local.dao.NotificationDao
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import com.example.lockedindungeon.data.local.entities.Notification

@Database(entities = [AppBlockingDetail::class, Notification::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appBlockingDetailDao(): AppBlockingDetailDao
    abstract fun notificationDao(): NotificationDao
}