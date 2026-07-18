package com.example.lockedindungeon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lockedindungeon.data.local.dao.PackageBlockingDetailDao
import com.example.lockedindungeon.data.local.enitities.PackageBlockingDetail

@Database(entities = [PackageBlockingDetail::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packageBlockingDetailDao(): PackageBlockingDetailDao
}