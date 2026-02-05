package com.pyera.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.pyera.app.data.repository.BudgetRepository
import com.pyera.app.data.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background worker for syncing app data with cloud.
 * Runs periodically to ensure local data is synchronized with the cloud.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync work... (attempt: $runAttemptCount)")
        
        return try {
            withContext(Dispatchers.IO) {
                var hasErrors = false
                
                // Sync pending transactions to cloud
                Log.d(TAG, "Syncing transactions...")
                val transactionResult = transactionRepository.syncPendingTransactions()
                if (transactionResult.isFailure) {
                    Log.e(TAG, "Failed to sync transactions", transactionResult.exceptionOrNull())
                    hasErrors = true
                }
                
                // Sync budget data
                Log.d(TAG, "Syncing budgets...")
                val budgetResult = budgetRepository.syncBudgets()
                if (budgetResult.isFailure) {
                    Log.e(TAG, "Failed to sync budgets", budgetResult.exceptionOrNull())
                    hasErrors = true
                }
                
                if (hasErrors && runAttemptCount < MAX_RETRY_COUNT) {
                    Log.w(TAG, "Sync completed with errors, will retry...")
                    Result.retry()
                } else {
                    Log.d(TAG, "Sync completed successfully")
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed with exception", e)
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    companion object {
        private const val TAG = "SyncWorker"
        private const val WORK_NAME = "sync_work"
        private const val MAX_RETRY_COUNT = 3
        
        /**
         * Sync interval: 1 hour
         */
        private const val SYNC_INTERVAL_HOURS = 1L
        
        /**
         * Flex interval: 15 minutes
         */
        private const val FLEX_INTERVAL_MINUTES = 15L
        
        /**
         * Schedule periodic sync work.
         * Should be called during app initialization.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
                SYNC_INTERVAL_HOURS, TimeUnit.HOURS,
                FLEX_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(SYNC_TAG)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncWork
            )
            
            Log.d(TAG, "Scheduled sync work to run every $SYNC_INTERVAL_HOURS hours")
        }
        
        /**
         * Cancel scheduled sync work.
         * Call this when user signs out or disables sync.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled sync work")
        }
        
        /**
         * Check if sync work is scheduled
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
         * Tag for all sync work
         */
        const val SYNC_TAG = "pyera_sync"
    }
}
