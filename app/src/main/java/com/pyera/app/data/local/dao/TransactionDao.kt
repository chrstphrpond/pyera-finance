package com.pyera.app.data.local.dao

import androidx.room.*
import com.pyera.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    
    // ==================== Standard Queries ====================
    
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsOnce(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND type = :type AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByCategoryAndTypeBetweenDates(
        categoryId: Int,
        type: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>>
    
    // ==================== Paginated Queries ====================
    
    /**
     * Get paginated transactions ordered by date descending
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPaged(limit: Int, offset: Int): List<TransactionEntity>
    
    /**
     * Get paginated transactions since a specific date
     * @param startDate Minimum date (epoch timestamp)
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM transactions WHERE date >= :startDate ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPagedSince(startDate: Long, limit: Int, offset: Int): List<TransactionEntity>
    
    /**
     * Get paginated transactions within a date range
     * @param startDate Start date (epoch timestamp)
     * @param endDate End date (epoch timestamp)
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPagedBetween(startDate: Long, endDate: Long, limit: Int, offset: Int): List<TransactionEntity>
    
    /**
     * Get paginated transactions by type
     * @param type Transaction type ("INCOME" or "EXPENSE")
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPagedByType(type: String, limit: Int, offset: Int): List<TransactionEntity>
    
    /**
     * Get paginated transactions by category
     * @param categoryId Category ID
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPagedByCategory(categoryId: Int, limit: Int, offset: Int): List<TransactionEntity>
    
    // ==================== Count Queries for Pagination ====================
    
    /**
     * Get total count of all transactions
     */
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int
    
    /**
     * Get count of transactions since a specific date
     * @param startDate Minimum date (epoch timestamp)
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE date >= :startDate")
    suspend fun getTransactionCountSince(startDate: Long): Int
    
    /**
     * Get count of transactions within a date range
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTransactionCountBetween(startDate: Long, endDate: Long): Int
    
    /**
     * Get count of transactions by type
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE type = :type")
    suspend fun getTransactionCountByType(type: String): Int
    
    /**
     * Get count of transactions by category
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun getTransactionCountByCategory(categoryId: Int): Int
}
