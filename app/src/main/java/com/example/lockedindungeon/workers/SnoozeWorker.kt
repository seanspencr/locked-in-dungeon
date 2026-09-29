package com.example.lockedindungeon.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class SnoozeWorker @Inject constructor(
    @ApplicationContext val appContext: Context,
    private val datastoreRepository: AppDatastoreRepository,
    workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    override fun doWork(): Result {

        // Do the work here--in this case, upload the images.
        activateBlocking()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    public fun activateBlocking(){
        CoroutineScope(Dispatchers.IO).launch{
            datastoreRepository.setActive(true)
        }
    }
}