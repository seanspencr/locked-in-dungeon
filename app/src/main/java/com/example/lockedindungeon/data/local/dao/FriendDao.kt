package com.example.lockedindungeon.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.lockedindungeon.data.local.entities.Friend

@Dao
interface FriendDao {

    @Query("SELECT * FROM friend")
    fun getAll(): List<Friend>

    @Query("SELECT * FROM friend WHERE userId IN (:userIds)")
    fun loadAllByIds(userIds: Array<String>): List<Friend>

    @Query("SELECT * FROM friend WHERE username LIKE :username LIMIT 1")
    fun findByName(username: String): Friend

    @Insert
    fun insertAll(vararg friend: Friend)

    @Insert
    fun insertOne(detail: Friend)

    @Delete
    fun delete(detail: Friend)
}