package com.example.lockedindungeon.data.local.repositories

import com.example.lockedindungeon.data.local.AppDatabase
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class PackageBlockingLocalRepository @Inject constructor(
    private var db : AppDatabase
) {

    companion object {
        private val tag : String = "PackageBlockingLocalRepository"
    }

    fun insertBlockingDetail(detail : AppBlockingDetail){
        return db.appBlockingDetailDao().insertOne(detail)
    }

    fun upsertBlockingDetail(detail : AppBlockingDetail){
        return db.appBlockingDetailDao().upsertOne(detail)
    }

    fun selectBlockingDetails(): Flow<List<AppBlockingDetail>?>? {
        return db.appBlockingDetailDao().getAll()
    }

    fun findBlockingDetail(packageName : String) : Flow<AppBlockingDetail?>? {
        return db.appBlockingDetailDao().findByName(packageName)
    }


}