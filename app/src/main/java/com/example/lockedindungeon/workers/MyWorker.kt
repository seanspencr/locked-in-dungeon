package com.example.lockedindungeon.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext



class MyWorker @AssistedInject constructor(@Assisted appContext: Context,
                                           @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {

        Log.d(TAG, "Performing long running task in scheduled job")
        return Result.success()
    }

    companion object {
        private val TAG = "MyWorker"
    }
}