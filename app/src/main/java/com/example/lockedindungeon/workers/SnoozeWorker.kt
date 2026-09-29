package com.example.lockedindungeon.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

@HiltWorker
class SnoozeWorker @AssistedInject constructor(@Assisted context : Context, @Assisted workerParams : WorkerParameters, private val datastoreRepository: AppDatastoreRepository)
: CoroutineWorker(appContext = context, params = workerParams) {



    private val tag = "SnoozeWorker"

    override suspend fun doWork(): Result {

        activateBlocking()

        return Result.success()
    }

    private suspend fun activateBlocking() {
        Log.d(tag, "Activating blocking")
        if (datastoreRepository.isActive.first()) return
        datastoreRepository.setActive(true)
    }
}
