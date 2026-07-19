package com.example.lockedindungeon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lockedindungeon.data.local.dao.FriendDao
import com.example.lockedindungeon.data.local.dao.PackageBlockingDetailDao
import com.example.lockedindungeon.data.local.entities.Friend
import com.example.lockedindungeon.data.local.entities.PackageBlockingDetail

@Database(entities = [PackageBlockingDetail::class, Friend::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packageBlockingDetailDao(): PackageBlockingDetailDao
    abstract fun friendDao(): FriendDao
}