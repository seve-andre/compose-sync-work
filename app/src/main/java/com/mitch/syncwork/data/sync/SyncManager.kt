package com.mitch.syncwork.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mitch.syncwork.data.auth.UserPrefsDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class SyncManager(
    appContext: Context,
    workerParams: WorkerParameters,
    private val userPrefsDataSource: UserPrefsDataSource
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val syncData = userPrefsDataSource.data
                .map { SyncData(isLoggedIn = it.isLoggedIn, syncRate = it.syncRate) }
                .first()
            if (syncData.isLoggedIn) {
                val progressZero = workDataOf(Progress to 0)
                val progressQuarter = workDataOf(Progress to 25)
                val progressHalf = workDataOf(Progress to 50)
                val progressDone = workDataOf(Progress to 100)
                Log.d("SyncManager", "Performing sync")
                setProgress(progressZero)
                delay(1.seconds)
                setProgress(progressQuarter)
                delay(1.seconds)
                setProgress(progressHalf)
                delay(2.seconds)
                setProgress(progressDone)
                Log.d("SyncManager", "Sync complete")
                scheduleSync(applicationContext, delay = syncData.syncRate.duration)
                Result.success()
            } else {
                val outputData = Data.Builder()
                    .putString("error_message", "User must be logged in to perform sync.")
                    .build()
                Result.failure(outputData)
            }
        } catch (e: Exception) {
            Log.d("SyncManager", "Sync failed with $e")
            Result.failure()
        }
    }

    companion object {
        const val Progress = "Progress"
        const val WorkTag = "sync_manager_tag"

        private fun scheduleSync(context: Context, delay: Duration) {
            WorkManager.getInstance(context).apply {
                enqueue(buildRequest(delay = delay))
            }
        }

        private fun buildRequest(delay: Duration): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<SyncManager>()
                .setInitialDelay(duration = delay.toJavaDuration())
                .addTag(WorkTag)
                .build()
        }
    }
}

class SyncManagerFactory(
    private val userPrefsDataSource: UserPrefsDataSource
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {

        return when (workerClassName) {
            SyncManager::class.java.name ->
                SyncManager(appContext, workerParameters, userPrefsDataSource = userPrefsDataSource)

            else ->
                // Return null, so that the base class can delegate to the default WorkerFactory.
                null
        }

    }
}
