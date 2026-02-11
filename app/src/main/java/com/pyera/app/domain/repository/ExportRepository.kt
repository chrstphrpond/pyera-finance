package com.pyera.app.domain.repository

import com.pyera.app.data.local.entity.TransactionEntity
import java.io.OutputStream

/**
 * Data class representing export summary statistics
 */
data class ExportSummary(
    val startDate: Long,
    val endDate: Long,
    val totalIncome: Double,
    val totalExpense: Double,
    val netAmount: Double,
    val transactionCount: Int = 0
)

/**
 * Enum representing supported export formats
 */
enum class ExportFormat {
    EXCEL,
    PDF,
    CSV
}

/**
 * Sealed class representing export result
 */
sealed class ExportResult {
    data class Success(
        val fileName: String,
        val recordCount: Int,
        val format: ExportFormat
    ) : ExportResult()
    
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : ExportResult()
}

/**
 * Repository interface for exporting transaction data
 */
interface ExportRepository {
    /**
     * Export transactions to Excel format (.xlsx)
     * @param transactions List of transactions to export
     * @param outputStream Output stream to write the file to
     * @param summary Export summary statistics
     * @return Result containing success message or error
     */
    suspend fun exportToExcel(
        transactions: List<TransactionEntity>,
        outputStream: OutputStream,
        summary: ExportSummary
    ): ExportResult

    /**
     * Export transactions to PDF format
     * @param transactions List of transactions to export
     * @param outputStream Output stream to write the file to
     * @param summary Export summary statistics
     * @return Result containing success message or error
     */
    suspend fun exportToPdf(
        transactions: List<TransactionEntity>,
        outputStream: OutputStream,
        summary: ExportSummary
    ): ExportResult

    /**
     * Export transactions to CSV format
     * @param transactions List of transactions to export
     * @param outputStream Output stream to write the file to
     * @param summary Export summary statistics
     * @return Result containing success message or error
     */
    suspend fun exportToCsv(
        transactions: List<TransactionEntity>,
        outputStream: OutputStream,
        summary: ExportSummary
    ): ExportResult
    
    /**
     * Generate a filename for the export based on format and current date
     * @param format The export format
     * @return Generated filename
     */
    fun generateFileName(format: ExportFormat): String
}
