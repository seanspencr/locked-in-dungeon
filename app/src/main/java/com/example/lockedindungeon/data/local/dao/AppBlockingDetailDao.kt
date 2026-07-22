package com.example.lockedindungeon.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface AppBlockingDetailDao {
    @Query("SELECT * FROM blocking_detail")
    fun getAll(): Flow<List<AppBlockingDetail>?>?

    @Query("SELECT * FROM blocking_detail WHERE packageNameOrUrl IN (:packageNameOrUrls)")
    fun loadAllByIds(packageNameOrUrls: Array<String>): Flow<List<AppBlockingDetail>?>?

    @Query("SELECT * FROM blocking_detail WHERE packageNameOrUrl LIKE :packageNameOrUrl LIMIT 1")
    fun findByName(packageNameOrUrl: String): Flow<AppBlockingDetail?>?

    @Insert
    fun insertAll(vararg detail: AppBlockingDetail)

    @Insert
    fun insertOne(detail: AppBlockingDetail)

    @Upsert
    fun upsertOne(detail: AppBlockingDetail)

    @Upsert
    fun upsertAll(vararg details: AppBlockingDetail)

    @Delete
    fun delete(detail: AppBlockingDetail)
}