package com.example.lockedindungeon.data.local.repositories

import com.example.lockedindungeon.data.local.AppDatabase
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppBlockingRepository @Inject constructor(
    private val db : AppDatabase,
    private val applicationScope : CoroutineScope
) {

    companion object {
        private val tag : String = "PackageBlockingLocalRepository"
    }

    /**
     * The blocked packages and urls, kept hot on [applicationScope].
     *
     * Eagerly on purpose: the accessibility service and
     * [com.example.lockedindungeon.common.AppBlocker] read `.value` without being UI subscribers,
     * and a lazy StateFlow would hand them the empty initial value instead of the room snapshot.
     */
    val blockingDetails : StateFlow<List<AppBlockingDetail>> = db.appBlockingDetailDao()
        .getAll()
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun insertBlockingDetail(detail : AppBlockingDetail) {
        return db.appBlockingDetailDao().insertOne(detail)
    }

    fun upsertBlockingDetail(detail : AppBlockingDetail) {
        return db.appBlockingDetailDao().upsertOne(detail)
    }

    fun findBlockingDetail(packageName : String) : Flow<AppBlockingDetail?> {
        return db.appBlockingDetailDao().findByName(packageName)
    }

    fun deleteBlockingDetail(packageNameOrUrl : String) {
        return db.appBlockingDetailDao().delete(packageNameOrUrl)
    }
}
