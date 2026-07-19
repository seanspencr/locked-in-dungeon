package com.example.lockedindungeon.data.local.repositories

import com.example.lockedindungeon.data.local.AppDatabase
import com.example.lockedindungeon.data.local.entities.Friend
import javax.inject.Inject


class FriendLocalRepository @Inject constructor(
    val db: AppDatabase
) {

    companion object {
        private val tag : String = "FriendLocalRepository"
    }

    fun insertFriend(detail : Friend){
        return db.friendDao().insertOne(detail)
    }

    fun selectFriends(): List<Friend> {
        return db.friendDao().getAll()

    }

    fun findFriendByUsername(username : String) : Friend {
        return db.friendDao().findByName(username)
    }


}