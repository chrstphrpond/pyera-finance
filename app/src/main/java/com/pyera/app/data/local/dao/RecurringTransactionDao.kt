package com.pyera.app.data.local.dao

import androidx.room.*
import com.pyera.app.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for recurring transactions.
 * Provides CRUD operations and queries for recurring transaction management.
 */
@Dao
interface RecurringTransactionDao {

    /**
     * Get all recurring transactions as a Flow.
     * Automatically emits updates when data changes.
     */
    @Query("SELECT * FROM recurring_transactions ORDER BY createdAt DESC")
    fun getAllRecurring(): Flow<List<RecurringTransactionEntity>>

    /**
     * Get all recurring transactions once.
     * Useful for background workers.
     */
    @Query("SELECT * FROM recurring_transactions ORDER BY createdAt DESC")
    suspend fun getAllRecurringOnce(): List<RecurringTransactionEntity>

    /**
     * Get only active recurring transactions.
     */
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveRecurring(): Flow<List<RecurringTransactionEntity>>

    /**
     * Get recurring transactions that are due for processing.
     * Returns transactions where nextDueDate <= currentDate and isActive = true.
     */
    @Query("SELECT * FROM recurring_transactions WHERE nextDueDate <= :currentDate AND isActive = 1 ORDER BY nextDueDate ASC")
    suspend fun getDueRecurring(currentDate: Long): List<RecurringTransactionEntity>

    /**
     * Get a single recurring transaction by ID.
     */
    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getRecurringById(id: Long): RecurringTransactionEntity?

    /**
     * Get a single recurring transaction by ID as Flow.
     */
    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    fun getRecurringByIdFlow(id: Long): Flow<RecurringTransactionEntity?>

    /**
     * Insert a new recurring transaction.
     * Returns the ID of the inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(entity: RecurringTransactionEntity): Long

    /**
     * Update an existing recurring transaction.
     */
    @Update
    suspend fun updateRecurring(entity: RecurringTransactionEntity)

    /**
     * Delete a recurring transaction.
     */
    @Delete
    suspend fun deleteRecurring(entity: RecurringTransactionEntity)

    /**
     * Delete a recurring transaction by ID.
     */
    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteRecurringById(id: Long)

    /**
     * Update the next due date for a recurring transaction.
     * Used by the worker after processing a due transaction.
     */
    @Query("UPDATE recurring_transactions SET nextDueDate = :nextDate WHERE id = :id")
    suspend fun updateNextDueDate(id: Long, nextDate: Long)

    /**
     * Toggle the active status of a recurring transaction.
     */
    @Query("UPDATE recurring_transactions SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: Long, isActive: Boolean)

    /**
     * Get count of all recurring transactions.
     */
    @Query("SELECT COUNT(*) FROM recurring_transactions")
    suspend fun getRecurringCount(): Int

    /**
     * Get count of active recurring transactions.
     */
    @Query("SELECT COUNT(*) FROM recurring_transactions WHERE isActive = 1")
    suspend fun getActiveRecurringCount(): Int
}
