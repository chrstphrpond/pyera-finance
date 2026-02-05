package com.pyera.app.data.local.dao

import androidx.room.*
import com.pyera.app.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    
    // ==================== Standard Queries ====================
    
    @Query("SELECT * FROM savings_goals ORDER BY deadline ASC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>
    
    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getSavingsGoalById(id: Int): SavingsGoalEntity?
    
    @Query("SELECT * FROM savings_goals WHERE currentAmount >= targetAmount ORDER BY deadline ASC")
    fun getCompletedGoals(): Flow<List<SavingsGoalEntity>>
    
    @Query("SELECT * FROM savings_goals WHERE currentAmount < targetAmount ORDER BY deadline ASC")
    fun getActiveGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoals(goals: List<SavingsGoalEntity>)

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity)
    
    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteSavingsGoalById(id: Int)
    
    /**
     * Update current amount for a savings goal
     */
    @Query("UPDATE savings_goals SET currentAmount = :amount WHERE id = :id")
    suspend fun updateCurrentAmount(id: Int, amount: Double)
    
    /**
     * Add amount to current savings
     */
    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount WHERE id = :id")
    suspend fun addToSavings(id: Int, amount: Double)
    
    // ==================== Paginated Queries ====================
    
    /**
     * Get paginated savings goals ordered by deadline
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM savings_goals ORDER BY deadline ASC LIMIT :limit OFFSET :offset")
    suspend fun getSavingsGoalsPaged(limit: Int, offset: Int): List<SavingsGoalEntity>
    
    /**
     * Get paginated active goals (not yet completed)
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM savings_goals WHERE currentAmount < targetAmount ORDER BY deadline ASC LIMIT :limit OFFSET :offset")
    suspend fun getActiveGoalsPaged(limit: Int, offset: Int): List<SavingsGoalEntity>
    
    /**
     * Get paginated completed goals
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM savings_goals WHERE currentAmount >= targetAmount ORDER BY deadline DESC LIMIT :limit OFFSET :offset")
    suspend fun getCompletedGoalsPaged(limit: Int, offset: Int): List<SavingsGoalEntity>
    
    /**
     * Get paginated goals with deadline before a specific date
     * @param deadline Deadline threshold
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM savings_goals WHERE deadline <= :deadline ORDER BY deadline ASC LIMIT :limit OFFSET :offset")
    suspend fun getGoalsByDeadlinePaged(deadline: Long, limit: Int, offset: Int): List<SavingsGoalEntity>
    
    // ==================== Count Queries for Pagination ====================
    
    /**
     * Get total count of all savings goals
     */
    @Query("SELECT COUNT(*) FROM savings_goals")
    suspend fun getSavingsGoalCount(): Int
    
    /**
     * Get count of active (incomplete) goals
     */
    @Query("SELECT COUNT(*) FROM savings_goals WHERE currentAmount < targetAmount")
    suspend fun getActiveGoalCount(): Int
    
    /**
     * Get count of completed goals
     */
    @Query("SELECT COUNT(*) FROM savings_goals WHERE currentAmount >= targetAmount")
    suspend fun getCompletedGoalCount(): Int
    
    /**
     * Get total savings across all goals
     */
    @Query("SELECT COALESCE(SUM(currentAmount), 0) FROM savings_goals")
    suspend fun getTotalSavings(): Double
    
    /**
     * Get total target amount across all goals
     */
    @Query("SELECT COALESCE(SUM(targetAmount), 0) FROM savings_goals")
    suspend fun getTotalTargetAmount(): Double
}
