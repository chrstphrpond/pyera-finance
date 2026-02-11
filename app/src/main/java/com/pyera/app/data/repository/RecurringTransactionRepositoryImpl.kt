package com.pyera.app.data.repository

import com.pyera.app.domain.repository.*

import android.util.Log
import com.pyera.app.data.local.dao.RecurringTransactionDao
import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.RecurringTransactionEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.worker.calculateNextDueDate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of the RecurringTransactionRepository interface.
 * Handles all recurring transaction operations including processing due transactions.
 */
class RecurringTransactionRepositoryImpl @Inject constructor(
    private val recurringDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao
) : RecurringTransactionRepository {

    companion object {
        private const val TAG = "RecurringRepo"
    }

    override fun getAllRecurring(): Flow<List<RecurringTransactionEntity>> {
        return recurringDao.getAllRecurring()
    }

    override suspend fun getAllRecurringOnce(): List<RecurringTransactionEntity> {
        return recurringDao.getAllRecurringOnce()
    }

    override fun getActiveRecurring(): Flow<List<RecurringTransactionEntity>> {
        return recurringDao.getActiveRecurring()
    }

    override suspend fun getDueRecurring(currentDate: Long): List<RecurringTransactionEntity> {
        return recurringDao.getDueRecurring(currentDate)
    }

    override suspend fun getRecurringById(id: Long): RecurringTransactionEntity? {
        return recurringDao.getRecurringById(id)
    }

    override fun getRecurringByIdFlow(id: Long): Flow<RecurringTransactionEntity?> {
        return recurringDao.getRecurringByIdFlow(id)
    }

    override suspend fun addRecurring(entity: RecurringTransactionEntity): Long {
        Log.d(TAG, "Adding recurring transaction: ${entity.description}")
        return recurringDao.insertRecurring(entity)
    }

    override suspend fun updateRecurring(entity: RecurringTransactionEntity) {
        Log.d(TAG, "Updating recurring transaction: ${entity.id}")
        recurringDao.updateRecurring(entity)
    }

    override suspend fun deleteRecurring(entity: RecurringTransactionEntity) {
        Log.d(TAG, "Deleting recurring transaction: ${entity.id}")
        recurringDao.deleteRecurring(entity)
    }

    override suspend fun deleteRecurringById(id: Long) {
        Log.d(TAG, "Deleting recurring transaction by ID: $id")
        recurringDao.deleteRecurringById(id)
    }

    override suspend fun toggleActiveStatus(id: Long, isActive: Boolean) {
        Log.d(TAG, "Toggling recurring transaction $id to isActive=$isActive")
        recurringDao.updateActiveStatus(id, isActive)
    }

    override suspend fun processDueRecurring(
        recurring: RecurringTransactionEntity,
        transaction: TransactionEntity
    ) {
        Log.d(TAG, "Processing due recurring: ${recurring.id}")

        // 1. Insert the actual transaction
        transactionDao.insertTransaction(transaction)
        Log.d(TAG, "Created transaction from recurring: ${recurring.id}")

        // 2. Calculate the next due date
        val nextDueDate = recurring.calculateNextDueDate()

        // 3. Check if end date has been reached
        val shouldDeactivate = recurring.endDate != null && nextDueDate > recurring.endDate

        if (shouldDeactivate) {
            // Deactivate the recurring transaction as it has reached its end date
            recurringDao.updateActiveStatus(recurring.id, false)
            Log.d(TAG, "Recurring ${recurring.id} reached end date, deactivated")
        } else {
            // Update the next due date
            recurringDao.updateNextDueDate(recurring.id, nextDueDate)
            Log.d(TAG, "Updated next due date for recurring ${recurring.id} to $nextDueDate")
        }
    }

    override suspend fun getRecurringCount(): Int {
        return recurringDao.getRecurringCount()
    }

    override suspend fun getActiveRecurringCount(): Int {
        return recurringDao.getActiveRecurringCount()
    }
}
