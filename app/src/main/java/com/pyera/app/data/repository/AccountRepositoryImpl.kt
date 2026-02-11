package com.pyera.app.data.repository

import com.pyera.app.domain.repository.*

import androidx.room.withTransaction
import com.pyera.app.data.local.PyeraDatabase
import com.pyera.app.data.local.dao.AccountDao
import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.AccountType
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.local.entity.defaultIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val database: PyeraDatabase,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val authRepository: AuthRepository
) : AccountRepository {

    private val currentUserId: String
        get() = authRepository.currentUser?.uid ?: ""

    // ==================== Query Operations ====================

    override fun getAllAccounts(): Flow<List<AccountEntity>> {
        return accountDao.getAccountsByUser(currentUserId)
    }

    override fun getActiveAccounts(): Flow<List<AccountEntity>> {
        return accountDao.getActiveAccountsByUser(currentUserId)
    }

    override suspend fun getAccountById(id: Long): AccountEntity? {
        return accountDao.getAccountById(id)
    }

    override suspend fun getDefaultAccount(): AccountEntity? {
        return accountDao.getDefaultAccount(currentUserId)
    }

    override fun getAccountsByType(type: AccountType): Flow<List<AccountEntity>> {
        return accountDao.getAccountsByType(type.name)
    }

    override suspend fun getTotalBalance(): Double {
        return accountDao.getTotalBalance(currentUserId) ?: 0.0
    }

    // ==================== CRUD Operations ====================

    override suspend fun createAccount(
        name: String,
        type: AccountType,
        initialBalance: Double,
        currency: String,
        color: Int,
        icon: String,
        isDefault: Boolean
    ): Result<Long> {
        return try {
            // Validate name
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Account name cannot be empty"))
            }
            
            // Check if name is unique
            if (!validateAccountName(name)) {
                return Result.failure(IllegalArgumentException("Account name already exists"))
            }

            database.withTransaction {
                val account = AccountEntity(
                    userId = currentUserId,
                    name = name.trim(),
                    type = type,
                    balance = initialBalance,
                    currency = currency,
                    color = color,
                    icon = icon.ifBlank { type.defaultIcon() },
                    isDefault = isDefault,
                    isArchived = false
                )

                val id = accountDao.insertAccount(account)

                // If this is the first account or set as default, update default status
                if (isDefault || accountDao.getAccountCount(currentUserId) == 1) {
                    accountDao.setDefaultAccount(currentUserId, id)
                }

                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAccount(account: AccountEntity): Result<Unit> {
        return try {
            // Validate name uniqueness (excluding this account)
            if (!validateAccountName(account.name, account.id)) {
                return Result.failure(IllegalArgumentException("Account name already exists"))
            }
            
            accountDao.updateAccount(account.copy(updatedAt = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(id: Long): Result<Unit> {
        return try {
            // Check if account has transactions
            if (!canDeleteAccount(id)) {
                return Result.failure(IllegalStateException("Cannot delete account with transactions. Archive it instead."))
            }
            
            accountDao.deleteAccount(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Account Management ====================

    override suspend fun setDefaultAccount(id: Long): Result<Unit> {
        return try {
            accountDao.setDefaultAccount(currentUserId, id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun archiveAccount(id: Long): Result<Unit> {
        return try {
            // Check if it's the default account
            val account = accountDao.getAccountById(id)
            if (account?.isDefault == true) {
                return Result.failure(IllegalStateException("Cannot archive the default account. Set another account as default first."))
            }
            
            accountDao.setArchived(id, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unarchiveAccount(id: Long): Result<Unit> {
        return try {
            accountDao.setArchived(id, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBalance(id: Long, newBalance: Double): Result<Unit> {
        return try {
            accountDao.updateBalance(id, newBalance)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Transfer Operations ====================

    override suspend fun transferBetweenAccounts(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        description: String,
        date: Long
    ): Result<Unit> {
        return try {
            database.withTransaction {
                // Validation
                if (fromAccountId == toAccountId) {
                    return@withTransaction Result.failure(IllegalArgumentException("Cannot transfer to the same account"))
                }
                if (amount <= 0) {
                    return@withTransaction Result.failure(IllegalArgumentException("Transfer amount must be positive"))
                }

                val fromAccount = accountDao.getAccountById(fromAccountId)
                    ?: return@withTransaction Result.failure(IllegalArgumentException("Source account not found"))
                val toAccount = accountDao.getAccountById(toAccountId)
                    ?: return@withTransaction Result.failure(IllegalArgumentException("Destination account not found"))

                if (fromAccount.balance < amount) {
                    return@withTransaction Result.failure(IllegalArgumentException("Insufficient balance in source account"))
                }

                val timestamp = System.currentTimeMillis()
                val transferNote = description.ifBlank { "Transfer to ${toAccount.name}" }
                val receiveNote = description.ifBlank { "Transfer from ${fromAccount.name}" }

                // Create expense transaction for source account
                val expenseTransaction = TransactionEntity(
                    amount = amount,
                    note = transferNote,
                    date = date,
                    type = "EXPENSE",
                    categoryId = null, // Transfers don't have categories
                    accountId = fromAccountId,
                    userId = currentUserId,
                    isTransfer = true,
                    transferAccountId = toAccountId,
                    createdAt = timestamp,
                    updatedAt = timestamp
                )

                // Create income transaction for destination account
                val incomeTransaction = TransactionEntity(
                    amount = amount,
                    note = receiveNote,
                    date = date,
                    type = "INCOME",
                    categoryId = null,
                    accountId = toAccountId,
                    userId = currentUserId,
                    isTransfer = true,
                    transferAccountId = fromAccountId,
                    createdAt = timestamp,
                    updatedAt = timestamp
                )

                // Insert both transactions
                transactionDao.insertTransaction(expenseTransaction)
                transactionDao.insertTransaction(incomeTransaction)

                // Update balances
                accountDao.updateBalance(fromAccountId, fromAccount.balance - amount)
                accountDao.updateBalance(toAccountId, toAccount.balance + amount)

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Balance Calculation ====================

    override suspend fun recalculateBalance(accountId: Long): Result<Double> {
        return try {
            if (accountDao.getAccountById(accountId) == null) {
                return Result.failure(IllegalArgumentException("Account not found"))
            }
            
            // Calculate from transactions
            val income = transactionDao.getAccountIncomeSum(accountId) ?: 0.0
            val expenses = transactionDao.getAccountExpenseSum(accountId) ?: 0.0
            
            // New balance = income - expenses (transfers are included in both)
            val newBalance = income - expenses
            
            accountDao.updateBalance(accountId, newBalance)
            Result.success(newBalance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Validation ====================

    override suspend fun validateAccountName(name: String, excludeId: Long?): Boolean {
        if (name.isBlank()) return false

        val accounts = accountDao.getAccountsByUser(currentUserId).first()
        return accounts.none {
            it.name.equals(name.trim(), ignoreCase = true) && it.id != excludeId
        }
    }

    override suspend fun canDeleteAccount(id: Long): Boolean {
        return transactionDao.getTransactionCountByAccount(id) == 0
    }
}
