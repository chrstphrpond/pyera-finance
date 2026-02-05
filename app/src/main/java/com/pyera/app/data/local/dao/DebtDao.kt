package com.pyera.app.data.local.dao

import androidx.room.*
import com.pyera.app.data.local.entity.DebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    
    // ==================== Standard Queries ====================
    
    @Query("SELECT * FROM debts ORDER BY dueDate ASC")
    fun getAllDebts(): Flow<List<DebtEntity>>
    
    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Int): DebtEntity?
    
    @Query("SELECT * FROM debts WHERE isPaid = 0 ORDER BY dueDate ASC")
    fun getUnpaidDebts(): Flow<List<DebtEntity>>
    
    @Query("SELECT * FROM debts WHERE isPaid = 1 ORDER BY dueDate DESC")
    fun getPaidDebts(): Flow<List<DebtEntity>>
    
    @Query("SELECT * FROM debts WHERE type = :type ORDER BY dueDate ASC")
    fun getDebtsByType(type: String): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<DebtEntity>)

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)
    
    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: Int)
    
    // ==================== Paginated Queries ====================
    
    /**
     * Get paginated debts ordered by due date
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM debts ORDER BY dueDate ASC LIMIT :limit OFFSET :offset")
    suspend fun getDebtsPaged(limit: Int, offset: Int): List<DebtEntity>
    
    /**
     * Get paginated unpaid debts
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM debts WHERE isPaid = 0 ORDER BY dueDate ASC LIMIT :limit OFFSET :offset")
    suspend fun getUnpaidDebtsPaged(limit: Int, offset: Int): List<DebtEntity>
    
    /**
     * Get paginated debts by type
     * @param type Debt type ("PAYABLE" or "RECEIVABLE")
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM debts WHERE type = :type ORDER BY dueDate ASC LIMIT :limit OFFSET :offset")
    suspend fun getDebtsByTypePaged(type: String, limit: Int, offset: Int): List<DebtEntity>
    
    /**
     * Get paginated debts due before a specific date
     * @param dueDate Due date threshold
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM debts WHERE dueDate <= :dueDate AND isPaid = 0 ORDER BY dueDate ASC LIMIT :limit OFFSET :offset")
    suspend fun getOverdueDebtsPaged(dueDate: Long, limit: Int, offset: Int): List<DebtEntity>
    
    // ==================== Count Queries for Pagination ====================
    
    /**
     * Get total count of all debts
     */
    @Query("SELECT COUNT(*) FROM debts")
    suspend fun getDebtCount(): Int
    
    /**
     * Get count of unpaid debts
     */
    @Query("SELECT COUNT(*) FROM debts WHERE isPaid = 0")
    suspend fun getUnpaidDebtCount(): Int
    
    /**
     * Get count of debts by type
     */
    @Query("SELECT COUNT(*) FROM debts WHERE type = :type")
    suspend fun getDebtCountByType(type: String): Int
    
    /**
     * Get count of overdue debts
     */
    @Query("SELECT COUNT(*) FROM debts WHERE dueDate <= :dueDate AND isPaid = 0")
    suspend fun getOverdueDebtCount(dueDate: Long): Int
}
