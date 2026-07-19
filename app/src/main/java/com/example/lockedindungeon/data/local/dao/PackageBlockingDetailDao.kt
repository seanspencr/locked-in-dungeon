package com.example.lockedindungeon.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.lockedindungeon.data.local.entities.PackageBlockingDetail

@Dao
interface PackageBlockingDetailDao {
    @Query("SELECT * FROM blocking_detail")
    fun getAll(): List<PackageBlockingDetail>?

    @Query("SELECT * FROM blocking_detail WHERE packageName IN (:packageNames)")
    fun loadAllByIds(packageNames: Array<String>): List<PackageBlockingDetail>?

    @Query("SELECT * FROM blocking_detail WHERE packageName LIKE :packageName LIMIT 1")
    fun findByName(packageName: String): PackageBlockingDetail?

    @Insert
    fun insertAll(vararg detail: PackageBlockingDetail)

    @Insert
    fun insertOne(detail: PackageBlockingDetail)

    @Delete
    fun delete(detail: PackageBlockingDetail)
}