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
import com.pyera.app.data.local.entity.RecurringFrequency
import com.pyera.app.data.local.entity.RecurringTransactionEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.domain.repository.RecurringTransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Background worker that processes due recurring transactions.
 * Runs daily to check for recurring transactions that are due and creates actual transactions.
 */
@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recurringRepository: RecurringTransactionRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting recurring transaction processing... (attempt: $runAttemptCount)")

        return try {
            withContext(Dispatchers.IO) {
                val currentTime = System.currentTimeMillis()
                val dueTransactions = recurringRepository.getDueRecurring(currentTime)

                Log.d(TAG, "Found ${dueTransactions.size} due recurring transactions")

                var processedCount = 0
                var errorCount = 0

                for (recurring in dueTransactions) {
                    try {
                        processRecurringTransaction(recurring)
                        processedCount++
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process recurring transaction ${recurring.id}", e)
                        errorCount++
                    }
                }

                Log.d(TAG, "Processed $processedCount transactions, $errorCount errors")

                if (errorCount > 0 && runAttemptCount < MAX_RETRY_COUNT) {
                    Result.retry()
                } else {
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Recurring transaction processing failed", e)
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Process a single recurring transaction:
     * 1. Create the actual transaction
     * 2. Calculate and update the next due date
     */
    private suspend fun processRecurringTransaction(recurring: RecurringTransactionEntity) {
        Log.d(TAG, "Processing recurring transaction ${recurring.id}: ${recurring.description}")

        val userId = authRepository.currentUser?.uid
        if (userId.isNullOrBlank()) {
            Log.e(TAG, "Skipping recurring ${recurring.id}: user not authenticated")
            return
        }

        val accountId = recurring.accountId
        if (accountId == null) {
            Log.e(TAG, "Skipping recurring ${recurring.id}: account not set")
            return
        }

        // Create the actual transaction
        val transaction = TransactionEntity(
            amount = recurring.amount,
            note = recurring.description,
            date = recurring.nextDueDate,
            type = recurring.type.name,
            categoryId = recurring.categoryId?.toInt(),
            accountId = accountId,
            userId = userId
        )

        // Insert the transaction and update next due date
        recurringRepository.processDueRecurring(recurring, transaction)

        Log.d(TAG, "Created transaction for recurring ${recurring.id}, next due: ${recurring.getNextDueDate()}")
    }

    /**
     * Calculate the next due date based on frequency.
     */
    private fun RecurringTransactionEntity.getNextDueDate(): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = nextDueDate

        when (frequency) {
            RecurringFrequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurringFrequency.BIWEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
            RecurringFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            RecurringFrequency.QUARTERLY -> calendar.add(Calendar.MONTH, 3)
            RecurringFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }

        return calendar.timeInMillis
    }

    companion object {
        private const val TAG = "RecurringWorker"
        private const val WORK_NAME = "recurring_transaction_work"
        private const val MAX_RETRY_COUNT = 3

        /**
         * Work interval: 1 day (24 hours)
         */
        private const val REPEAT_INTERVAL_DAYS = 1L

        /**
         * Flex interval: 1 hour
         */
        private const val FLEX_INTERVAL_HOURS = 1L

        /**
         * Schedule periodic work to process recurring transactions.
         * Should be called during app initialization.
         */
        fun schedule(context: Context) {
            // Constraints: Run even without network, but require battery not low
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val recurringWork = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
                REPEAT_INTERVAL_DAYS, TimeUnit.DAYS,
                FLEX_INTERVAL_HOURS, TimeUnit.HOURS
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
                recurringWork
            )

            Log.d(TAG, "Scheduled recurring transaction work to run every $REPEAT_INTERVAL_DAYS days")
        }

        /**
         * Cancel scheduled recurring transaction work.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled recurring transaction work")
        }

        /**
         * Check if recurring transaction work is scheduled.
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
         * Tag for all recurring transaction work.
         */
        const val WORK_TAG = "pyera_recurring"
    }
}

/**
 * Extension function to calculate next due date for a recurring transaction.
 */
fun RecurringTransactionEntity.calculateNextDueDate(): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = nextDueDate

    when (frequency) {
        RecurringFrequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
        RecurringFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
        RecurringFrequency.BIWEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
        RecurringFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
        RecurringFrequency.QUARTERLY -> calendar.add(Calendar.MONTH, 3)
        RecurringFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
    }

    return calendar.timeInMillis
}
