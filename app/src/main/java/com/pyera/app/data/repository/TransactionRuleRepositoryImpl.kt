package com.pyera.app.data.repository

import com.pyera.app.domain.repository.*

import com.pyera.app.data.local.dao.TransactionRuleDao
import com.pyera.app.data.local.entity.MatchType
import com.pyera.app.data.local.entity.TransactionRuleEntity
import com.pyera.app.data.local.entity.matches
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [TransactionRuleRepository].
 * Manages transaction categorization rules with priority-based matching.
 */
@Singleton
class TransactionRuleRepositoryImpl @Inject constructor(
    private val transactionRuleDao: TransactionRuleDao
) : TransactionRuleRepository {

    override fun getAllRules(userId: String): Flow<List<TransactionRuleEntity>> {
        return transactionRuleDao.getAllRules(userId)
    }

    override suspend fun getAllRulesSync(userId: String): List<TransactionRuleEntity> {
        return transactionRuleDao.getAllRulesSync(userId)
    }

    override suspend fun getActiveRules(userId: String): List<TransactionRuleEntity> {
        return transactionRuleDao.getActiveRules(userId)
    }

    override suspend fun getRulesByCategory(categoryId: Int): List<TransactionRuleEntity> {
        return transactionRuleDao.getRulesByCategory(categoryId)
    }

    override suspend fun getRuleById(ruleId: Long): TransactionRuleEntity? {
        return transactionRuleDao.getRuleById(ruleId)
    }

    override suspend fun createRule(
        userId: String,
        pattern: String,
        matchType: MatchType,
        categoryId: Int,
        priority: Int
    ): Result<Long> {
        return try {
            val trimmedPattern = pattern.trim()
            if (trimmedPattern.isBlank()) {
                return Result.failure(IllegalArgumentException("Pattern cannot be empty"))
            }

            val rule = TransactionRuleEntity(
                userId = userId,
                pattern = trimmedPattern,
                matchType = matchType.name,
                categoryId = categoryId,
                priority = priority.coerceIn(0, 100)
            )

            val ruleId = transactionRuleDao.insertRule(rule)
            Result.success(ruleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRule(rule: TransactionRuleEntity): Result<Unit> {
        return try {
            val updatedRule = rule.copy(
                pattern = rule.pattern.trim(),
                priority = rule.priority.coerceIn(0, 100),
                updatedAt = System.currentTimeMillis()
            )
            transactionRuleDao.updateRule(updatedRule)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRule(ruleId: Long): Result<Unit> {
        return try {
            transactionRuleDao.deleteRuleById(ruleId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleRuleActive(ruleId: Long, isActive: Boolean): Result<Unit> {
        return try {
            transactionRuleDao.setRuleActive(ruleId, isActive)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun applyRulesToTransaction(userId: String, description: String): Int? {
        if (description.isBlank()) return null

        val activeRules = getActiveRules(userId)
        
        // Rules are already sorted by priority DESC from the DAO query
        for (rule in activeRules) {
            if (rule.matches(description)) {
                return rule.categoryId
            }
        }
        
        return null
    }

    override fun testRule(pattern: String, matchType: MatchType, testDescription: String): Boolean {
        if (pattern.isBlank() || testDescription.isBlank()) return false

        val descLower = testDescription.lowercase()
        val patternLower = pattern.lowercase().trim()

        return when (matchType) {
            MatchType.CONTAINS -> descLower.contains(patternLower)
            MatchType.STARTS_WITH -> descLower.startsWith(patternLower)
            MatchType.ENDS_WITH -> descLower.endsWith(patternLower)
            MatchType.EXACT -> descLower == patternLower
            MatchType.REGEX -> {
                runCatching {
                    descLower.matches(Regex(pattern, RegexOption.IGNORE_CASE))
                }.getOrDefault(false)
            }
        }
    }

    override suspend fun createRuleFromTransaction(
        userId: String,
        description: String,
        categoryId: Int,
        matchType: MatchType
    ): Result<Long> {
        // Extract a meaningful pattern from the description
        // Use the first 3-4 words or the whole description if it's short
        val words = description.trim().split(Regex("\\s+"))
        val pattern = when {
            words.size <= 2 -> description.trim()
            words.size <= 4 -> words.take(words.size - 1).joinToString(" ")
            else -> words.take(3).joinToString(" ")
        }

        return createRule(
            userId = userId,
            pattern = pattern,
            matchType = matchType,
            categoryId = categoryId,
            priority = 5 // Default medium priority for auto-created rules
        )
    }
}
