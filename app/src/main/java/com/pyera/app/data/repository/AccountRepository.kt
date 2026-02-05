package com.pyera.app.data.repository

import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.AccountType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing financial accounts.
 * Provides CRUD operations and business logic for account management.
 */
interface AccountRepository {

    // ==================== Query Operations ====================

    /**
     * Get all accounts for the current user
     */
    fun getAllAccounts(): Flow<List<AccountEntity>>

    /**
     * Get all active (non-archived) accounts
     */
    fun getActiveAccounts(): Flow<List<AccountEntity>>

    /**
     * Get a specific account by ID
     */
    suspend fun getAccountById(id: Long): AccountEntity?

    /**
     * Get the default account for the current user
     */
    suspend fun getDefaultAccount(): AccountEntity?

    /**
     * Get accounts filtered by type
     */
    fun getAccountsByType(type: AccountType): Flow<List<AccountEntity>>

    /**
     * Get total balance across all active accounts
     */
    suspend fun getTotalBalance(): Double

    // ==================== CRUD Operations ====================

    /**
     * Create a new account
     * @return ID of the created account
     */
    suspend fun createAccount(
        name: String,
        type: AccountType,
        initialBalance: Double = 0.0,
        currency: String = "PHP",
        color: Int,
        icon: String,
        isDefault: Boolean = false
    ): Result<Long>

    /**
     * Update an existing account
     */
    suspend fun updateAccount(account: AccountEntity): Result<Unit>

    /**
     * Delete an account by ID
     * Note: Cannot delete accounts with transactions
     */
    suspend fun deleteAccount(id: Long): Result<Unit>

    // ==================== Account Management ====================

    /**
     * Set an account as the default account
     */
    suspend fun setDefaultAccount(id: Long): Result<Unit>

    /**
     * Archive an account (soft delete)
     */
    suspend fun archiveAccount(id: Long): Result<Unit>

    /**
     * Unarchive an account
     */
    suspend fun unarchiveAccount(id: Long): Result<Unit>

    /**
     * Update account balance directly
     */
    suspend fun updateBalance(id: Long, newBalance: Double): Result<Unit>

    // ==================== Transfer Operations ====================

    /**
     * Transfer funds between accounts
     * Creates two transaction records: one expense from source, one income to destination
     * @param fromAccountId Source account ID
     * @param toAccountId Destination account ID
     * @param amount Amount to transfer
     * @param description Optional description
     * @param date Optional timestamp (defaults to now)
     * @return Result indicating success or failure
     */
    suspend fun transferBetweenAccounts(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        description: String = "",
        date: Long = System.currentTimeMillis()
    ): Result<Unit>

    // ==================== Balance Calculation ====================

    /**
     * Recalculate account balance from transactions
     * Useful for correcting balance drift or initial setup
     */
    suspend fun recalculateBalance(accountId: Long): Result<Double>

    // ==================== Validation ====================

    /**
     * Check if account name is valid and unique
     */
    suspend fun validateAccountName(name: String, excludeId: Long? = null): Boolean

    /**
     * Check if an account can be deleted (has no transactions)
     */
    suspend fun canDeleteAccount(id: Long): Boolean
}
