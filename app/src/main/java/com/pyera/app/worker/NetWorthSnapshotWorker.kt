package com.pyera.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.pyera.app.data.repository.NetWorthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Background worker that saves net worth snapshots at the end of each month.
 * Runs monthly to capture the user's financial position for historical tracking.
 */
@HiltWorker
class NetWorthSnapshotWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val netWorthRepository: NetWorthRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting net worth snapshot work... (attempt: $runAttemptCount)")

        return try {
            withContext(Dispatchers.IO) {
                // Calculate and save the current snapshot
                val result = netWorthRepository.saveCurrentSnapshot()

                if (result.isSuccess) {
                    Log.d(TAG, "Net worth snapshot saved successfully with id: ${result.getOrNull()}")
                    Result.success()
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Failed to save net worth snapshot", error)
                    
                    if (runAttemptCount < MAX_RETRY_COUNT) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Net worth snapshot work failed", e)
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "NetWorthSnapshotWorker"
        private const val WORK_NAME = "net_worth_snapshot_work"
        private const val MAX_RETRY_COUNT = 3

        /**
         * Tag for all net worth snapshot work.
         */
        const val WORK_TAG = "pyera_networth_snapshot"

        /**
         * Schedule monthly work to save net worth snapshots.
         * Should be called during app initialization.
         */
        fun schedule(context: Context) {
            // Constraints: Run when battery is not low
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            // Schedule monthly work (approximately every 30 days)
            val monthlyWork = PeriodicWorkRequestBuilder<NetWorthSnapshotWorker>(
                30, TimeUnit.DAYS,  // Repeat every 30 days
                7, TimeUnit.DAYS    // Flex interval of 7 days
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                monthlyWork
            )

            Log.d(TAG, "Scheduled monthly net worth snapshot work")
        }

        /**
         * Schedule a one-time immediate snapshot
         */
        fun scheduleImmediate(context: Context) {
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<NetWorthSnapshotWorker>()
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d(TAG, "Scheduled immediate net worth snapshot")
        }

        /**
         * Cancel scheduled net worth snapshot work.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled net worth snapshot work")
        }

        /**
         * Check if net worth snapshot work is scheduled.
         */
        fun isScheduled(context: Context): Boolean {
            val workManager = WorkManager.getInstance(context)
            val workInfos = workManager.getWorkInfosForUniqueWork(WORK_NAME).get()
            return workInfos?.any {
                it.state == androidx.work.WorkInfo.State.ENQUEUED ||
                it.state == androidx.work.WorkInfo.State.RUNNING
            } ?: false
        }

        /**
         * Get the optimal time to run the monthly snapshot (end of month).
         * Returns the delay in milliseconds until the last day of the current month.
         */
        fun getEndOfMonthDelay(): Long {
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            // If we're already past the 25th, schedule for next month
            return if (currentDay >= 25) {
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 2) // 2 AM
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis - System.currentTimeMillis()
            } else {
                // Schedule for end of this month
                calendar.set(Calendar.DAY_OF_MONTH, lastDay)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 55)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis - System.currentTimeMillis()
            }
        }
    }
}
