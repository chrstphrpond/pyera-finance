package com.pyera.app.data.repository

import com.pyera.app.domain.repository.*

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pyera.app.data.local.dao.AccountDao
import com.pyera.app.data.local.dao.NetWorthDao
import com.pyera.app.data.local.entity.AccountBreakdownEntry
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.AccountType
import com.pyera.app.data.local.entity.NetWorthSnapshot
import com.pyera.app.data.local.entity.formattedBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetWorthRepositoryImpl @Inject constructor(
    private val netWorthDao: NetWorthDao,
    private val accountDao: AccountDao,
    private val authRepository: AuthRepository,
    private val gson: Gson
) : NetWorthRepository {

    private val currentUserId: String
        get() = authRepository.currentUser?.uid ?: ""

    companion object {
        private const val TAG = "NetWorthRepository"
        
        // Account types that are considered liabilities
        private val LIABILITY_TYPES = setOf(AccountType.CREDIT_CARD)
    }

    // ==================== Snapshot Operations ====================

    override fun getAllSnapshots(): Flow<List<NetWorthSnapshot>> {
        return netWorthDao.getSnapshots(currentUserId)
    }

    override fun getSnapshotsForPeriod(startDate: Long, endDate: Long): Flow<List<NetWorthSnapshot>> {
        return netWorthDao.getSnapshotsForPeriod(currentUserId, startDate, endDate)
    }

    override suspend fun getLatestSnapshot(): NetWorthSnapshot? {
        return netWorthDao.getLatestSnapshot(currentUserId)
    }

    override fun getLatestSnapshotFlow(): Flow<NetWorthSnapshot?> {
        return netWorthDao.getLatestSnapshotFlow(currentUserId)
    }

    override suspend fun getSnapshotForMonth(date: Long): NetWorthSnapshot? {
        return netWorthDao.getSnapshotForMonth(currentUserId, date)
    }

    override fun getNetWorthHistory(months: Int): Flow<List<NetWorthSnapshot>> {
        val since = getMonthsAgo(months)
        return netWorthDao.getSnapshotsSince(currentUserId, since)
    }

    // ==================== Calculation Operations ====================

    override suspend fun calculateCurrentNetWorth(): CurrentNetWorth {
        val accounts = accountDao.getActiveAccountsByUser(currentUserId).first()
        
        val assetAccounts = accounts.filter { it.type !in LIABILITY_TYPES }
        val liabilityAccounts = accounts.filter { it.type in LIABILITY_TYPES }
        
        val totalAssets = assetAccounts.sumOf { it.balance }
        val totalLiabilities = liabilityAccounts.sumOf { kotlin.math.abs(it.balance) }
        
        return CurrentNetWorth(
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            netWorth = totalAssets - totalLiabilities,
            assetAccounts = assetAccounts,
            liabilityAccounts = liabilityAccounts
        )
    }

    override suspend fun saveMonthlySnapshot(date: Long): Result<Long> {
        return try {
            val currentNetWorth = calculateCurrentNetWorth()
            
            // Create account breakdown JSON
            val allAccounts = currentNetWorth.assetAccounts + currentNetWorth.liabilityAccounts
            val breakdownEntries = allAccounts.map { account ->
                AccountBreakdownEntry(
                    accountId = account.id,
                    accountName = account.name,
                    accountType = account.type,
                    balance = account.balance,
                    isAsset = account.type !in LIABILITY_TYPES
                )
            }
            
            val snapshot = NetWorthSnapshot(
                userId = currentUserId,
                date = date,
                totalAssets = currentNetWorth.totalAssets,
                totalLiabilities = currentNetWorth.totalLiabilities,
                netWorth = currentNetWorth.netWorth,
                accountsBreakdown = gson.toJson(breakdownEntries)
            )
            
            val id = netWorthDao.insertSnapshot(snapshot)
            Log.d(TAG, "Saved monthly snapshot with id: $id for date: $date")
            Result.success(id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save monthly snapshot", e)
            Result.failure(e)
        }
    }

    override suspend fun saveCurrentSnapshot(): Result<Long> {
        return saveMonthlySnapshot(getCurrentMonthStart())
    }

    // ==================== Growth & Analysis ====================

    override suspend fun getMonthOverMonthGrowth(): NetWorthGrowth? {
        val currentSnapshot = getLatestSnapshot() ?: return null
        
        // Get the snapshot from the previous month
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentSnapshot.date
        calendar.add(Calendar.MONTH, -1)
        val previousMonthStart = calendar.timeInMillis
        
        val previousSnapshot = netWorthDao.getSnapshotForMonth(currentUserId, previousMonthStart)
            ?: return NetWorthGrowth(
                currentNetWorth = currentSnapshot.netWorth,
                previousNetWorth = 0.0,
                absoluteChange = currentSnapshot.netWorth,
                percentageChange = 0.0,
                period = "month"
            )
        
        val absoluteChange = currentSnapshot.netWorth - previousSnapshot.netWorth
        val percentageChange = if (previousSnapshot.netWorth != 0.0) {
            (absoluteChange / kotlin.math.abs(previousSnapshot.netWorth)) * 100
        } else 0.0
        
        return NetWorthGrowth(
            currentNetWorth = currentSnapshot.netWorth,
            previousNetWorth = previousSnapshot.netWorth,
            absoluteChange = absoluteChange,
            percentageChange = percentageChange,
            period = "month"
        )
    }

    override suspend fun getYearOverYearGrowth(): NetWorthGrowth? {
        val currentSnapshot = getLatestSnapshot() ?: return null
        
        // Get the snapshot from the previous year
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentSnapshot.date
        calendar.add(Calendar.YEAR, -1)
        val previousYearStart = calendar.timeInMillis
        
        val previousSnapshot = netWorthDao.getSnapshotForMonth(currentUserId, previousYearStart)
            ?: return NetWorthGrowth(
                currentNetWorth = currentSnapshot.netWorth,
                previousNetWorth = 0.0,
                absoluteChange = currentSnapshot.netWorth,
                percentageChange = 0.0,
                period = "year"
            )
        
        val absoluteChange = currentSnapshot.netWorth - previousSnapshot.netWorth
        val percentageChange = if (previousSnapshot.netWorth != 0.0) {
            (absoluteChange / kotlin.math.abs(previousSnapshot.netWorth)) * 100
        } else 0.0
        
        return NetWorthGrowth(
            currentNetWorth = currentSnapshot.netWorth,
            previousNetWorth = previousSnapshot.netWorth,
            absoluteChange = absoluteChange,
            percentageChange = percentageChange,
            period = "year"
        )
    }

    override suspend fun getNetWorthGrowth(months: Int): NetWorthGrowth? {
        val snapshots = netWorthDao.getSnapshotsSince(currentUserId, getMonthsAgo(months)).first()
        
        if (snapshots.size < 2) {
            val latest = snapshots.firstOrNull() ?: return null
            return NetWorthGrowth(
                currentNetWorth = latest.netWorth,
                previousNetWorth = 0.0,
                absoluteChange = latest.netWorth,
                percentageChange = 0.0,
                period = "$months months"
            )
        }
        
        val current = snapshots.first()
        val oldest = snapshots.last()
        
        val absoluteChange = current.netWorth - oldest.netWorth
        val percentageChange = if (oldest.netWorth != 0.0) {
            (absoluteChange / kotlin.math.abs(oldest.netWorth)) * 100
        } else 0.0
        
        return NetWorthGrowth(
            currentNetWorth = current.netWorth,
            previousNetWorth = oldest.netWorth,
            absoluteChange = absoluteChange,
            percentageChange = percentageChange,
            period = "$months months"
        )
    }

    override suspend fun getAssetAllocation(): List<AssetAllocation> {
        val currentNetWorth = calculateCurrentNetWorth()
        val totalAssets = currentNetWorth.totalAssets
        
        if (totalAssets <= 0) return emptyList()
        
        return currentNetWorth.assetAccounts
            .groupBy { it.type }
            .map { (type, accounts) ->
                val amount = accounts.sumOf { it.balance }
                AssetAllocation(
                    type = type,
                    amount = amount,
                    percentage = (amount / totalAssets) * 100,
                    accountCount = accounts.size
                )
            }
            .sortedByDescending { it.amount }
    }

    override suspend fun getLiabilityBreakdown(): List<AssetAllocation> {
        val currentNetWorth = calculateCurrentNetWorth()
        val totalLiabilities = currentNetWorth.totalLiabilities
        
        if (totalLiabilities <= 0) return emptyList()
        
        return currentNetWorth.liabilityAccounts
            .groupBy { it.type }
            .map { (type, accounts) ->
                val amount = accounts.sumOf { kotlin.math.abs(it.balance) }
                AssetAllocation(
                    type = type,
                    amount = amount,
                    percentage = (amount / totalLiabilities) * 100,
                    accountCount = accounts.size
                )
            }
            .sortedByDescending { it.amount }
    }

    // ==================== CRUD Operations ====================

    override suspend fun insertSnapshot(snapshot: NetWorthSnapshot): Result<Long> {
        return try {
            val id = netWorthDao.insertSnapshot(snapshot.copy(userId = currentUserId))
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSnapshot(id: Long): Result<Unit> {
        return try {
            netWorthDao.deleteSnapshot(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllSnapshots(): Result<Unit> {
        return try {
            netWorthDao.deleteAllSnapshotsForUser(currentUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Export ====================

    override suspend fun exportToCsv(): Result<String> {
        return try {
            val snapshots = netWorthDao.getSnapshots(currentUserId).first()
            
            if (snapshots.isEmpty()) {
                return Result.failure(IllegalStateException("No snapshots to export"))
            }
            
            val csvBuilder = StringBuilder()
            csvBuilder.appendLine("Date,Total Assets,Total Liabilities,Net Worth")
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            snapshots.reversed().forEach { snapshot ->
                csvBuilder.appendLine(
                    "${dateFormat.format(snapshot.date)}," +
                    "${snapshot.totalAssets},${snapshot.totalLiabilities},${snapshot.netWorth}"
                )
            }
            
            Result.success(csvBuilder.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getShareableSummary(): String {
        val currentNetWorth = calculateCurrentNetWorth()
        val latestSnapshot = getLatestSnapshot()
        val monthGrowth = getMonthOverMonthGrowth()
        
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        
        return buildString {
            appendLine("📊 My Net Worth Summary")
            appendLine()
            appendLine("💰 Net Worth: ${String.format("₱%,.2f", currentNetWorth.netWorth)}")
            appendLine("📈 Assets: ${String.format("₱%,.2f", currentNetWorth.totalAssets)}")
            appendLine("📉 Liabilities: ${String.format("₱%,.2f", currentNetWorth.totalLiabilities)}")
            
            monthGrowth?.let {
                appendLine()
                val changeEmoji = if (it.absoluteChange >= 0) "📈" else "📉"
                appendLine("$changeEmoji Month Change: ${String.format("%,.2f", it.absoluteChange)} (${String.format("%.1f", it.percentageChange)}%)")
            }
            
            latestSnapshot?.let {
                appendLine()
                appendLine("Last updated: ${dateFormat.format(it.createdAt)}")
            }
            
            appendLine()
            appendLine("Tracked with Pyera Finance")
        }
    }
}
