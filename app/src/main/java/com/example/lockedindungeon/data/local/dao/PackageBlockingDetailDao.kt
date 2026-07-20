package com.example.lockedindungeon.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.lockedindungeon.data.local.entities.PackageBlockingDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageBlockingDetailDao {
    @Query("SELECT * FROM blocking_detail")
    fun getAll(): Flow<List<PackageBlockingDetail>?>?

    @Query("SELECT * FROM blocking_detail WHERE packageName IN (:packageNames)")
    fun loadAllByIds(packageNames: Array<String>): Flow<List<PackageBlockingDetail>?>?

    @Query("SELECT * FROM blocking_detail WHERE packageName LIKE :packageName LIMIT 1")
    fun findByName(packageName: String): Flow<PackageBlockingDetail?>?

    @Insert
    fun insertAll(vararg detail: PackageBlockingDetail)

    @Insert
    fun insertOne(detail: PackageBlockingDetail)

    @Upsert
    fun upsertOne(detail: PackageBlockingDetail)

    @Upsert
    fun upsertAll(vararg details: PackageBlockingDetail)

    @Delete
    fun delete(detail: PackageBlockingDetail)
}