package com.pyera.app.data.local.dao

import androidx.room.*
import com.pyera.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    // ==================== Standard Queries ====================
    
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategoriesSorted(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Int): Flow<CategoryEntity>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryByIdSync(id: Int): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE type = :type")
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByTypeSorted(type: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
    
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?
    
    // ==================== Paginated Queries ====================
    
    /**
     * Get paginated categories ordered by name
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM categories ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getCategoriesPaged(limit: Int, offset: Int): List<CategoryEntity>
    
    /**
     * Get paginated categories by type
     * @param type Category type ("INCOME" or "EXPENSE")
     * @param limit Number of items to fetch
     * @param offset Number of items to skip
     */
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getCategoriesByTypePaged(type: String, limit: Int, offset: Int): List<CategoryEntity>
    
    // ==================== Count Queries for Pagination ====================
    
    /**
     * Get total count of all categories
     */
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
    
    /**
     * Get count of categories by type
     */
    @Query("SELECT COUNT(*) FROM categories WHERE type = :type")
    suspend fun getCategoryCountByType(type: String): Int
}
