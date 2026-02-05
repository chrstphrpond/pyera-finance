package com.pyera.app.data.local.dao

import androidx.room.*
import com.pyera.app.data.local.entity.NetWorthSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for NetWorthSnapshot entity.
 * Provides CRUD operations and queries for managing net worth snapshots.
 */
@Dao
interface NetWorthDao {

    // ==================== Query Operations ====================

    /**
     * Get all net worth snapshots for a user, ordered by date (newest first)
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId ORDER BY date DESC")
    fun getSnapshots(userId: String): Flow<List<NetWorthSnapshot>>

    /**
     * Get all net worth snapshots for a user, ordered by date (oldest first)
     * Useful for trend calculations
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId ORDER BY date ASC")
    fun getSnapshotsOldestFirst(userId: String): Flow<List<NetWorthSnapshot>>

    /**
     * Get snapshots for a specific date range
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSnapshotsForPeriod(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<NetWorthSnapshot>>

    /**
     * Get the most recent snapshot for a user
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestSnapshot(userId: String): NetWorthSnapshot?

    /**
     * Get the most recent snapshot for a user as Flow
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    fun getLatestSnapshotFlow(userId: String): Flow<NetWorthSnapshot?>

    /**
     * Get snapshot for a specific month
     * @param date The first day of the month
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getSnapshotForMonth(userId: String, date: Long): NetWorthSnapshot?

    /**
     * Get snapshot for a specific month as Flow
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId AND date = :date LIMIT 1")
    fun getSnapshotForMonthFlow(userId: String, date: Long): Flow<NetWorthSnapshot?>

    /**
     * Get the oldest snapshot for a user
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId ORDER BY date ASC LIMIT 1")
    suspend fun getOldestSnapshot(userId: String): NetWorthSnapshot?

    /**
     * Get snapshots count for a user
     */
    @Query("SELECT COUNT(*) FROM net_worth_snapshots WHERE userId = :userId")
    suspend fun getSnapshotCount(userId: String): Int

    /**
     * Get snapshots for the last N months
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId AND date >= :since ORDER BY date DESC")
    fun getSnapshotsSince(userId: String, since: Long): Flow<List<NetWorthSnapshot>>

    // ==================== Insert Operations ====================

    /**
     * Insert a new snapshot. Returns the ID of the newly inserted snapshot.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: NetWorthSnapshot): Long

    /**
     * Insert multiple snapshots
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshots(snapshots: List<NetWorthSnapshot>)

    // ==================== Update Operations ====================

    /**
     * Update an existing snapshot
     */
    @Update
    suspend fun updateSnapshot(snapshot: NetWorthSnapshot)

    // ==================== Delete Operations ====================

    /**
     * Delete a snapshot by ID
     */
    @Query("DELETE FROM net_worth_snapshots WHERE id = :id")
    suspend fun deleteSnapshot(id: Long)

    /**
     * Delete a snapshot entity
     */
    @Delete
    suspend fun deleteSnapshot(snapshot: NetWorthSnapshot)

    /**
     * Delete all snapshots for a user
     */
    @Query("DELETE FROM net_worth_snapshots WHERE userId = :userId")
    suspend fun deleteAllSnapshotsForUser(userId: String)

    /**
     * Delete snapshots older than a specific date
     */
    @Query("DELETE FROM net_worth_snapshots WHERE userId = :userId AND date < :date")
    suspend fun deleteSnapshotsOlderThan(userId: String, date: Long)

    // ==================== Aggregate Operations ====================

    /**
     * Get average net worth for a user
     */
    @Query("SELECT AVG(netWorth) FROM net_worth_snapshots WHERE userId = :userId")
    suspend fun getAverageNetWorth(userId: String): Double?

    /**
     * Get highest net worth for a user
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId ORDER BY netWorth DESC LIMIT 1")
    suspend fun getHighestNetWorth(userId: String): NetWorthSnapshot?

    /**
     * Get lowest net worth for a user
     */
    @Query("SELECT * FROM net_worth_snapshots WHERE userId = :userId ORDER BY netWorth ASC LIMIT 1")
    suspend fun getLowestNetWorth(userId: String): NetWorthSnapshot?

    /**
     * Get total growth (current - first) for a user
     */
    @Query("""
        SELECT (latest.netWorth - first.netWorth) as growth 
        FROM 
            (SELECT netWorth FROM net_worth_snapshots WHERE userId = :userId ORDER BY date DESC LIMIT 1) as latest,
            (SELECT netWorth FROM net_worth_snapshots WHERE userId = :userId ORDER BY date ASC LIMIT 1) as first
    """)
    suspend fun getTotalGrowth(userId: String): Double?
}
