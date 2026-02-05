package com.pyera.app.data.repository

import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.local.entity.TransactionTemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for transaction template operations
 */
interface TransactionTemplateRepository {

    /**
     * Get all templates for a user as a Flow for reactive updates
     */
    suspend fun getAllTemplates(userId: String): Flow<List<TransactionTemplateEntity>>

    /**
     * Get most used templates for quick access
     */
    suspend fun getMostUsedTemplates(userId: String, limit: Int = 10): List<TransactionTemplateEntity>

    /**
     * Get active templates only
     */
    suspend fun getActiveTemplates(userId: String): Flow<List<TransactionTemplateEntity>>

    /**
     * Get a single template by ID
     */
    suspend fun getTemplateById(templateId: Long): TransactionTemplateEntity?

    /**
     * Create a new template
     */
    suspend fun createTemplate(
        userId: String,
        name: String,
        description: String,
        amount: Double?,
        type: String,
        categoryId: Int?,
        accountId: Long?,
        icon: String? = null,
        color: Int? = null
    ): Result<Long>

    /**
     * Create a template from an existing transaction
     */
    suspend fun createTemplateFromTransaction(
        userId: String,
        transaction: TransactionEntity,
        name: String,
        icon: String? = null
    ): Result<Long>

    /**
     * Update an existing template
     */
    suspend fun updateTemplate(template: TransactionTemplateEntity): Result<Unit>

    /**
     * Delete a template
     */
    suspend fun deleteTemplate(templateId: Long): Result<Unit>

    /**
     * Mark a template as used (increments usage count)
     */
    suspend fun useTemplate(templateId: Long): Result<Unit>

    /**
     * Toggle template active status
     */
    suspend fun toggleActive(templateId: Long, isActive: Boolean): Result<Unit>

    /**
     * Reorder templates by updating display order
     */
    suspend fun reorderTemplates(orderedIds: List<Long>): Result<Unit>

    /**
     * Search templates by name
     */
    suspend fun searchTemplates(userId: String, query: String): List<TransactionTemplateEntity>
}
