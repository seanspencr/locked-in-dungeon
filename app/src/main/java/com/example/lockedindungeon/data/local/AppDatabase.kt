package com.example.lockedindungeon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lockedindungeon.data.local.dao.FriendDao
import com.example.lockedindungeon.data.local.dao.AppBlockingDetailDao
import com.example.lockedindungeon.data.local.entities.Friend
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail

@Database(entities = [AppBlockingDetail::class, Friend::class], version = 4)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appBlockingDetailDao(): AppBlockingDetailDao
    abstract fun friendDao(): FriendDao
}