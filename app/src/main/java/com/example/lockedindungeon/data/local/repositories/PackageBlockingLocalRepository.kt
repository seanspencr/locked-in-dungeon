package com.example.lockedindungeon.data.local.repositories

import com.example.lockedindungeon.data.local.AppDatabase
import com.example.lockedindungeon.data.local.entities.PackageBlockingDetail
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class PackageBlockingLocalRepository @Inject constructor(
    private var db : AppDatabase
) {

    companion object {
        private val tag : String = "PackageBlockingLocalRepository"
    }

    fun insertBlockingDetail(detail : PackageBlockingDetail){
        return db.packageBlockingDetailDao().insertOne(detail)
    }

    fun upsertBlockingDetail(detail : PackageBlockingDetail){
        return db.packageBlockingDetailDao().upsertOne(detail)
    }

    fun selectBlockingDetails(): Flow<List<PackageBlockingDetail>?>? {
        return db.packageBlockingDetailDao().getAll()
    }

    fun findBlockingDetail(packageName : String) : Flow<PackageBlockingDetail?>? {
        return db.packageBlockingDetailDao().findByName(packageName)
    }


}