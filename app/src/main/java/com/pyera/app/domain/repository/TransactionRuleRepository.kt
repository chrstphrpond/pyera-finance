package com.pyera.app.domain.repository

import com.pyera.app.data.local.entity.MatchType
import com.pyera.app.data.local.entity.TransactionRuleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for transaction rules operations.
 * Provides methods to manage user-defined categorization rules.
 */
interface TransactionRuleRepository {
    /**
     * Get all rules for the current user as a Flow.
     */
    fun getAllRules(userId: String): Flow<List<TransactionRuleEntity>>

    /**
     * Get all rules for the current user as a one-time query.
     */
    suspend fun getAllRulesSync(userId: String): List<TransactionRuleEntity>

    /**
     * Get only active rules for applying to transactions.
     */
    suspend fun getActiveRules(userId: String): List<TransactionRuleEntity>

    /**
     * Get rules that target a specific category.
     */
    suspend fun getRulesByCategory(categoryId: Int): List<TransactionRuleEntity>

    /**
     * Get a single rule by ID.
     */
    suspend fun getRuleById(ruleId: Long): TransactionRuleEntity?

    /**
     * Create a new rule.
     * @return Result containing the new rule ID on success
     */
    suspend fun createRule(
        userId: String,
        pattern: String,
        matchType: MatchType,
        categoryId: Int,
        priority: Int = 0
    ): Result<Long>

    /**
     * Update an existing rule.
     */
    suspend fun updateRule(rule: TransactionRuleEntity): Result<Unit>

    /**
     * Delete a rule by ID.
     */
    suspend fun deleteRule(ruleId: Long): Result<Unit>

    /**
     * Toggle a rule's active status.
     */
    suspend fun toggleRuleActive(ruleId: Long, isActive: Boolean): Result<Unit>

    /**
     * Apply rules to a transaction description to find the best matching category.
     * Rules are checked in priority order (highest first).
     * @return The category ID of the first matching rule, or null if no match
     */
    suspend fun applyRulesToTransaction(userId: String, description: String): Int?

    /**
     * Test if a pattern matches a description without saving the rule.
     * @return true if the pattern matches the description
     */
    fun testRule(pattern: String, matchType: MatchType, testDescription: String): Boolean

    /**
     * Create a rule from an existing transaction's description.
     * Convenience method for "Save as Rule" feature.
     */
    suspend fun createRuleFromTransaction(
        userId: String,
        description: String,
        categoryId: Int,
        matchType: MatchType = MatchType.CONTAINS
    ): Result<Long>
}
