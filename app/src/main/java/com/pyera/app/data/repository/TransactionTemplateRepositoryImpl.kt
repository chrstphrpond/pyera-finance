package com.pyera.app.data.repository

import com.pyera.app.data.local.dao.TransactionTemplateDao
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.local.entity.TransactionTemplateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TransactionTemplateRepository
 */
@Singleton
class TransactionTemplateRepositoryImpl @Inject constructor(
    private val templateDao: TransactionTemplateDao
) : TransactionTemplateRepository {

    override suspend fun getAllTemplates(userId: String): Flow<List<TransactionTemplateEntity>> {
        return templateDao.getAllTemplates(userId)
    }

    override suspend fun getMostUsedTemplates(
        userId: String,
        limit: Int
    ): List<TransactionTemplateEntity> {
        return templateDao.getMostUsedTemplates(userId, limit)
    }

    override suspend fun getActiveTemplates(userId: String): Flow<List<TransactionTemplateEntity>> {
        return templateDao.getActiveTemplates(userId)
    }

    override suspend fun getTemplateById(templateId: Long): TransactionTemplateEntity? {
        return templateDao.getTemplateById(templateId)
    }

    override suspend fun createTemplate(
        userId: String,
        name: String,
        description: String,
        amount: Double?,
        type: String,
        categoryId: Int?,
        accountId: Long?,
        icon: String?,
        color: Int?
    ): Result<Long> {
        return try {
            val template = TransactionTemplateEntity(
                userId = userId,
                name = name,
                description = description,
                amount = amount,
                type = type,
                categoryId = categoryId,
                accountId = accountId,
                icon = icon,
                color = color,
                displayOrder = templateDao.getTemplateCount(userId) // Add to end
            )
            val id = templateDao.insertTemplate(template)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTemplateFromTransaction(
        userId: String,
        transaction: TransactionEntity,
        name: String,
        icon: String?
    ): Result<Long> {
        return try {
            val template = TransactionTemplateEntity(
                userId = userId,
                name = name,
                description = transaction.note,
                amount = transaction.amount,
                type = transaction.type,
                categoryId = transaction.categoryId,
                accountId = transaction.accountId,
                icon = icon,
                displayOrder = templateDao.getTemplateCount(userId)
            )
            val id = templateDao.insertTemplate(template)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTemplate(template: TransactionTemplateEntity): Result<Unit> {
        return try {
            templateDao.updateTemplate(template)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTemplate(templateId: Long): Result<Unit> {
        return try {
            templateDao.deleteTemplateById(templateId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun useTemplate(templateId: Long): Result<Unit> {
        return try {
            templateDao.incrementUsage(templateId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleActive(templateId: Long, isActive: Boolean): Result<Unit> {
        return try {
            templateDao.setActive(templateId, isActive)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reorderTemplates(orderedIds: List<Long>): Result<Unit> {
        return try {
            orderedIds.forEachIndexed { index, templateId ->
                templateDao.updateDisplayOrder(templateId, index)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchTemplates(
        userId: String,
        query: String
    ): List<TransactionTemplateEntity> {
        return templateDao.searchTemplates(userId, query)
    }
}
