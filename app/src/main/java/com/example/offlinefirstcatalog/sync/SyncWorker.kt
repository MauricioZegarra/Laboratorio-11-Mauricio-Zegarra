package com.example.offlinefirstcatalog.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.offlinefirstcatalog.data.SyncResult
import com.example.offlinefirstcatalog.di.AppContainer

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = AppContainer.getRepository(applicationContext)

        return when (val result = repository.fullSync()) {
            is SyncResult.Success -> Result.success()
            is SyncResult.NoConnection -> Result.success()
            is SyncResult.Error -> {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }
}