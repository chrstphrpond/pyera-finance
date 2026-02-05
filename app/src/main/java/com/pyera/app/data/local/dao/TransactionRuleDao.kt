package com.pyera.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pyera.app.data.local.entity.TransactionRuleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for transaction rules operations.
 * Provides methods to create, read, update, and delete transaction categorization rules.
 */
@Dao
interface TransactionRuleDao {

    /**
     * Get all rules for a user as a Flow for reactive updates.
     */
    @Query("SELECT * FROM transaction_rules WHERE userId = :userId ORDER BY priority DESC, createdAt DESC")
    fun getAllRules(userId: String): Flow<List<TransactionRuleEntity>>

    /**
     * Get all rules for a user as a one-time query.
     */
    @Query("SELECT * FROM transaction_rules WHERE userId = :userId ORDER BY priority DESC, createdAt DESC")
    suspend fun getAllRulesSync(userId: String): List<TransactionRuleEntity>

    /**
     * Get only active rules for a user, ordered by priority (highest first).
     * Used when applying rules to new transactions.
     */
    @Query("SELECT * FROM transaction_rules WHERE userId = :userId AND isActive = 1 ORDER BY priority DESC")
    suspend fun getActiveRules(userId: String): List<TransactionRuleEntity>

    /**
     * Get all rules that target a specific category.
     */
    @Query("SELECT * FROM transaction_rules WHERE categoryId = :categoryId ORDER BY priority DESC")
    suspend fun getRulesByCategory(categoryId: Int): List<TransactionRuleEntity>

    /**
     * Get a single rule by its ID.
     */
    @Query("SELECT * FROM transaction_rules WHERE id = :ruleId LIMIT 1")
    suspend fun getRuleById(ruleId: Long): TransactionRuleEntity?

    /**
     * Insert a new rule and return its auto-generated ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: TransactionRuleEntity): Long

    /**
     * Update an existing rule.
     */
    @Update
    suspend fun updateRule(rule: TransactionRuleEntity)

    /**
     * Delete a rule.
     */
    @Delete
    suspend fun deleteRule(rule: TransactionRuleEntity)

    /**
     * Delete a rule by its ID.
     */
    @Query("DELETE FROM transaction_rules WHERE id = :ruleId")
    suspend fun deleteRuleById(ruleId: Long)

    /**
     * Toggle the active status of a rule.
     */
    @Query("UPDATE transaction_rules SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :ruleId")
    suspend fun setRuleActive(ruleId: Long, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * Get the count of rules for a user.
     */
    @Query("SELECT COUNT(*) FROM transaction_rules WHERE userId = :userId")
    suspend fun getRuleCount(userId: String): Int

    /**
     * Delete all rules for a user.
     */
    @Query("DELETE FROM transaction_rules WHERE userId = :userId")
    suspend fun deleteAllRulesForUser(userId: String)
}
