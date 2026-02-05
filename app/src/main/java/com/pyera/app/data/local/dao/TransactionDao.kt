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
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

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
    
    // ==================== Account-related Queries ====================
    
    /**
     * Get all transactions for a specific account
     */
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>
    
    /**
     * Get recent transactions for a specific account (limited)
     */
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentTransactionsByAccount(accountId: Long, limit: Int): List<TransactionEntity>
    
    /**
     * Get sum of income for an account
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId AND type = 'INCOME'")
    suspend fun getAccountIncomeSum(accountId: Long): Double?
    
    /**
     * Get sum of expenses for an account
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId AND type = 'EXPENSE'")
    suspend fun getAccountExpenseSum(accountId: Long): Double?
    
    /**
     * Get sum of outgoing transfers for an account
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId AND isTransfer = 1")
    suspend fun getAccountTransferOutSum(accountId: Long): Double?
    
    /**
     * Get sum of incoming transfers for an account
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE transferAccountId = :accountId AND isTransfer = 1")
    suspend fun getAccountTransferInSum(accountId: Long): Double?
    
    /**
     * Count transactions for an account
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun getTransactionCountByAccount(accountId: Long): Int
    
    // ==================== Transfer Queries ====================
    
    /**
     * Get all transfer transactions
     */
    @Query("SELECT * FROM transactions WHERE isTransfer = 1 ORDER BY date DESC")
    fun getTransferTransactions(): Flow<List<TransactionEntity>>
    
    /**
     * Get transfers between two accounts
     */
    @Query("SELECT * FROM transactions WHERE isTransfer = 1 AND ((accountId = :fromAccount AND transferAccountId = :toAccount) OR (accountId = :toAccount AND transferAccountId = :fromAccount)) ORDER BY date DESC")
    fun getTransfersBetweenAccounts(fromAccount: Long, toAccount: Long): Flow<List<TransactionEntity>>
    
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
    
    /**
     * Get paginated transactions by account
     * @param accountId Account ID
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPagedByAccount(accountId: Long, limit: Int, offset: Int): List<TransactionEntity>
    
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
