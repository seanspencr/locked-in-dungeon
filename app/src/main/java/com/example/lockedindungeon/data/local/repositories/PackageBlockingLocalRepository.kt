package com.example.lockedindungeon.data.local.repositories

import android.content.Context
import androidx.room.Room
import com.example.lockedindungeon.data.local.AppDatabase
import com.example.lockedindungeon.data.local.entities.PackageBlockingDetail
import javax.inject.Inject


class PackageBlockingLocalRepository @Inject constructor(
    val db: AppDatabase
) {

    companion object {
        private val tag : String = "PackageBlockingLocalRepository"
    }

    fun insertBlockingDetail(detail : PackageBlockingDetail){
        return db.packageBlockingDetailDao().insertOne(detail)
    }

    fun selectBlockingDetail(): List<PackageBlockingDetail>? {
        return db.packageBlockingDetailDao().getAll()
    }

    fun findBlockingDetail(packageName : String) : PackageBlockingDetail? {
        return db.packageBlockingDetailDao().findByName(packageName)
    }


}