package com.pyera.app.data.local.dao

import androidx.room.*
import com.pyera.app.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for AccountEntity.
 * Provides CRUD operations and queries for managing financial accounts.
 */
@Dao
interface AccountDao {

    // ==================== Query Operations ====================

    /**
     * Get all accounts ordered by creation date (newest first)
     */
    @Query("SELECT * FROM accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    /**
     * Get all accounts for a specific user
     */
    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY isDefault DESC, createdAt DESC")
    fun getAccountsByUser(userId: String): Flow<List<AccountEntity>>

    /**
     * Get all non-archived (active) accounts
     */
    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY isDefault DESC, createdAt DESC")
    fun getActiveAccounts(): Flow<List<AccountEntity>>

    /**
     * Get active accounts for a specific user
     */
    @Query("SELECT * FROM accounts WHERE userId = :userId AND isArchived = 0 ORDER BY isDefault DESC, createdAt DESC")
    fun getActiveAccountsByUser(userId: String): Flow<List<AccountEntity>>

    /**
     * Get a specific account by ID
     */
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    /**
     * Get the default account for a user
     */
    @Query("SELECT * FROM accounts WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultAccount(userId: String): AccountEntity?

    /**
     * Get accounts by type
     */
    @Query("SELECT * FROM accounts WHERE type = :type AND isArchived = 0 ORDER BY createdAt DESC")
    fun getAccountsByType(type: String): Flow<List<AccountEntity>>

    /**
     * Get total balance across all active accounts for a user
     */
    @Query("SELECT SUM(balance) FROM accounts WHERE userId = :userId AND isArchived = 0")
    suspend fun getTotalBalance(userId: String): Double?

    // ==================== Insert Operations ====================

    /**
     * Insert a new account. Returns the ID of the newly inserted account.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: AccountEntity): Long

    // ==================== Update Operations ====================

    /**
     * Update an existing account
     */
    @Update
    suspend fun updateAccount(account: AccountEntity)

    /**
     * Update account balance directly
     */
    @Query("UPDATE accounts SET balance = :newBalance, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, newBalance: Double, timestamp: Long = System.currentTimeMillis())

    /**
     * Set an account as default and unset any existing default for the user
     */
    @Transaction
    suspend fun setDefaultAccount(userId: String, accountId: Long) {
        // First, unset current default
        unsetDefaultForUser(userId)
        // Then set the new default
        setAccountAsDefault(accountId, System.currentTimeMillis())
    }

    @Query("UPDATE accounts SET isDefault = 0 WHERE userId = :userId")
    suspend fun unsetDefaultForUser(userId: String)

    @Query("UPDATE accounts SET isDefault = 1, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun setAccountAsDefault(accountId: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * Archive or unarchive an account
     */
    @Query("UPDATE accounts SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun setArchived(accountId: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    // ==================== Delete Operations ====================

    /**
     * Delete an account by ID
     */
    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    /**
     * Delete an account entity
     */
    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    // ==================== Count Operations ====================

    /**
     * Get count of all accounts for a user
     */
    @Query("SELECT COUNT(*) FROM accounts WHERE userId = :userId")
    suspend fun getAccountCount(userId: String): Int

    /**
     * Get count of active (non-archived) accounts for a user
     */
    @Query("SELECT COUNT(*) FROM accounts WHERE userId = :userId AND isArchived = 0")
    suspend fun getActiveAccountCount(userId: String): Int
}
