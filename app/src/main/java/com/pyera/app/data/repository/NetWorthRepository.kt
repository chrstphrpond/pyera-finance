package com.pyera.app.data.repository

import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.AccountType
import com.pyera.app.data.local.entity.NetWorthSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Data class representing asset allocation breakdown
 */
data class AssetAllocation(
    val type: AccountType,
    val amount: Double,
    val percentage: Double,
    val accountCount: Int
)

/**
 * Data class representing net worth growth metrics
 */
data class NetWorthGrowth(
    val currentNetWorth: Double,
    val previousNetWorth: Double,
    val absoluteChange: Double,
    val percentageChange: Double,
    val period: String // e.g., "month", "quarter", "year"
)

/**
 * Data class representing current net worth calculation
 */
data class CurrentNetWorth(
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double,
    val assetAccounts: List<AccountEntity>,
    val liabilityAccounts: List<AccountEntity>
)

/**
 * Repository interface for net worth tracking operations.
 */
interface NetWorthRepository {

    // ==================== Snapshot Operations ====================

    /**
     * Get all net worth snapshots for the current user
     */
    fun getAllSnapshots(): Flow<List<NetWorthSnapshot>>

    /**
     * Get snapshots for a specific date range
     */
    fun getSnapshotsForPeriod(startDate: Long, endDate: Long): Flow<List<NetWorthSnapshot>>

    /**
     * Get the most recent snapshot
     */
    suspend fun getLatestSnapshot(): NetWorthSnapshot?

    /**
     * Get the most recent snapshot as Flow
     */
    fun getLatestSnapshotFlow(): Flow<NetWorthSnapshot?>

    /**
     * Get snapshot for a specific month
     */
    suspend fun getSnapshotForMonth(date: Long): NetWorthSnapshot?

    /**
     * Get snapshots for the last N months
     */
    fun getNetWorthHistory(months: Int): Flow<List<NetWorthSnapshot>>

    // ==================== Calculation Operations ====================

    /**
     * Calculate current net worth from all accounts
     */
    suspend fun calculateCurrentNetWorth(): CurrentNetWorth

    /**
     * Save a monthly snapshot
     * @param date The first day of the month
     */
    suspend fun saveMonthlySnapshot(date: Long = getCurrentMonthStart()): Result<Long>

    /**
     * Save current net worth as a snapshot
     */
    suspend fun saveCurrentSnapshot(): Result<Long>

    // ==================== Growth & Analysis ====================

    /**
     * Get month-over-month growth
     */
    suspend fun getMonthOverMonthGrowth(): NetWorthGrowth?

    /**
     * Get year-over-year growth
     */
    suspend fun getYearOverYearGrowth(): NetWorthGrowth?

    /**
     * Get net worth growth over a specific period
     */
    suspend fun getNetWorthGrowth(months: Int): NetWorthGrowth?

    /**
     * Get asset allocation breakdown by account type
     */
    suspend fun getAssetAllocation(): List<AssetAllocation>

    /**
     * Get liability breakdown by account type
     */
    suspend fun getLiabilityBreakdown(): List<AssetAllocation>

    // ==================== CRUD Operations ====================

    /**
     * Insert a snapshot manually
     */
    suspend fun insertSnapshot(snapshot: NetWorthSnapshot): Result<Long>

    /**
     * Delete a snapshot by ID
     */
    suspend fun deleteSnapshot(id: Long): Result<Unit>

    /**
     * Delete all snapshots for the current user
     */
    suspend fun deleteAllSnapshots(): Result<Unit>

    // ==================== Export ====================

    /**
     * Export net worth history as CSV
     */
    suspend fun exportToCsv(): Result<String>

    /**
     * Get net worth summary for sharing
     */
    suspend fun getShareableSummary(): String
}

/**
 * Helper function to get the first day of the current month
 */
fun getCurrentMonthStart(): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/**
 * Helper function to get the first day of a specific month
 */
fun getMonthStart(year: Int, month: Int): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(year, month - 1, 1, 0, 0, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/**
 * Helper function to get months ago timestamp
 */
fun getMonthsAgo(months: Int): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.MONTH, -months)
    return calendar.timeInMillis
}
