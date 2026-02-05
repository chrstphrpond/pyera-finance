package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a net worth snapshot at a specific point in time.
 * Used to track net worth history and trends over time.
 */
@Entity(
    tableName = "net_worth_snapshots",
    indices = [
        Index(value = ["userId"], name = "idx_networth_user"),
        Index(value = ["date"], name = "idx_networth_date"),
        Index(value = ["userId", "date"], name = "idx_networth_user_date")
    ]
)
data class NetWorthSnapshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val date: Long, // Month start date (first day of month at midnight)
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double, // Calculated: assets - liabilities
    val accountsBreakdown: String, // JSON of account balances by account type
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Data class representing account breakdown entry for JSON serialization
 */
data class AccountBreakdownEntry(
    val accountId: Long,
    val accountName: String,
    val accountType: AccountType,
    val balance: Double,
    val isAsset: Boolean // true for asset accounts, false for liability accounts
)

/**
 * Extension function to calculate net worth from assets and liabilities
 */
fun NetWorthSnapshot.calculateNetWorth(): Double {
    return totalAssets - totalLiabilities
}

/**
 * Extension function to get percentage change from another snapshot
 */
fun NetWorthSnapshot.percentageChangeFrom(previous: NetWorthSnapshot?): Double {
    if (previous == null || previous.netWorth == 0.0) return 0.0
    return ((netWorth - previous.netWorth) / kotlin.math.abs(previous.netWorth)) * 100
}

/**
 * Extension function to format net worth as currency string
 */
fun NetWorthSnapshot.formattedNetWorth(): String {
    return when {
        netWorth >= 0 -> "₱${String.format("%,.2f", netWorth)}"
        else -> "-₱${String.format("%,.2f", kotlin.math.abs(netWorth))}"
    }
}

/**
 * Extension function to format assets as currency string
 */
fun NetWorthSnapshot.formattedAssets(): String {
    return "₱${String.format("%,.2f", totalAssets)}"
}

/**
 * Extension function to format liabilities as currency string
 */
fun NetWorthSnapshot.formattedLiabilities(): String {
    return "₱${String.format("%,.2f", totalLiabilities)}"
}
