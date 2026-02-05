package com.pyera.app.data.repository

import com.pyera.app.data.local.entity.RecurringTransactionEntity
import com.pyera.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for recurring transactions.
 * Provides abstraction for CRUD operations and business logic.
 */
interface RecurringTransactionRepository {
    /**
     * Get all recurring transactions as a Flow.
     */
    fun getAllRecurring(): Flow<List<RecurringTransactionEntity>>

    /**
     * Get all recurring transactions once (for background operations).
     */
    suspend fun getAllRecurringOnce(): List<RecurringTransactionEntity>

    /**
     * Get only active recurring transactions.
     */
    fun getActiveRecurring(): Flow<List<RecurringTransactionEntity>>

    /**
     * Get recurring transactions due for processing.
     * @param currentDate The current timestamp to compare against
     */
    suspend fun getDueRecurring(currentDate: Long): List<RecurringTransactionEntity>

    /**
     * Get a single recurring transaction by ID.
     */
    suspend fun getRecurringById(id: Long): RecurringTransactionEntity?

    /**
     * Get a single recurring transaction by ID as Flow.
     */
    fun getRecurringByIdFlow(id: Long): Flow<RecurringTransactionEntity?>

    /**
     * Add a new recurring transaction.
     * @param entity The recurring transaction to add
     * @return The ID of the inserted entity
     */
    suspend fun addRecurring(entity: RecurringTransactionEntity): Long

    /**
     * Update an existing recurring transaction.
     */
    suspend fun updateRecurring(entity: RecurringTransactionEntity)

    /**
     * Delete a recurring transaction.
     */
    suspend fun deleteRecurring(entity: RecurringTransactionEntity)

    /**
     * Delete a recurring transaction by ID.
     */
    suspend fun deleteRecurringById(id: Long)

    /**
     * Toggle the active status of a recurring transaction.
     */
    suspend fun toggleActiveStatus(id: Long, isActive: Boolean)

    /**
     * Process a due recurring transaction:
     * 1. Create the actual transaction
     * 2. Calculate and update the next due date
     * 3. Check if end date has been reached
     */
    suspend fun processDueRecurring(
        recurring: RecurringTransactionEntity,
        transaction: TransactionEntity
    )

    /**
     * Get count of all recurring transactions.
     */
    suspend fun getRecurringCount(): Int

    /**
     * Get count of active recurring transactions.
     */
    suspend fun getActiveRecurringCount(): Int
}
