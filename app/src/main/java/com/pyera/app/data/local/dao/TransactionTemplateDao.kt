package com.pyera.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pyera.app.data.local.entity.TransactionTemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for transaction template operations
 */
@Dao
interface TransactionTemplateDao {

    @Query("SELECT * FROM transaction_templates WHERE userId = :userId ORDER BY displayOrder ASC, useCount DESC, createdAt DESC")
    fun getAllTemplates(userId: String): Flow<List<TransactionTemplateEntity>>

    @Query("SELECT * FROM transaction_templates WHERE userId = :userId AND isActive = 1 ORDER BY useCount DESC, lastUsedAt DESC LIMIT :limit")
    suspend fun getMostUsedTemplates(userId: String, limit: Int = 10): List<TransactionTemplateEntity>

    @Query("SELECT * FROM transaction_templates WHERE userId = :userId AND isActive = 1 ORDER BY displayOrder ASC")
    fun getActiveTemplates(userId: String): Flow<List<TransactionTemplateEntity>>

    @Query("SELECT * FROM transaction_templates WHERE id = :templateId LIMIT 1")
    suspend fun getTemplateById(templateId: Long): TransactionTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TransactionTemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: TransactionTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: TransactionTemplateEntity)

    @Query("DELETE FROM transaction_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Long)

    @Query("UPDATE transaction_templates SET useCount = useCount + 1, lastUsedAt = :timestamp WHERE id = :templateId")
    suspend fun incrementUsage(templateId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE transaction_templates SET isActive = :isActive WHERE id = :templateId")
    suspend fun setActive(templateId: Long, isActive: Boolean)

    @Query("UPDATE transaction_templates SET displayOrder = :order WHERE id = :templateId")
    suspend fun updateDisplayOrder(templateId: Long, order: Int)

    @Query("SELECT COUNT(*) FROM transaction_templates WHERE userId = :userId")
    suspend fun getTemplateCount(userId: String): Int

    @Query("SELECT * FROM transaction_templates WHERE userId = :userId AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchTemplates(userId: String, query: String): List<TransactionTemplateEntity>
}
