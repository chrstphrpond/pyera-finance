package com.pyera.app.data.export

import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.repository.ExportSummary
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for exporting transactions to CSV format
 */
class CsvExporter {

    companion object {
        private const val CSV_HEADER = "Date,Time,Type,Category,Amount,Account,Description,Transaction ID"
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val TIME_FORMAT = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    }

    /**
     * Export transactions to CSV format
     * @param transactions List of transactions to export
     * @param outputStream Output stream to write to
     * @param summary Export summary statistics (included as comments at top)
     * @param categoryNames Map of category IDs to category names
     * @param accountNames Map of account IDs to account names
     */
    fun export(
        transactions: List<TransactionEntity>,
        outputStream: OutputStream,
        summary: ExportSummary,
        categoryNames: Map<Int, String>,
        accountNames: Map<Long, String>
    ) {
        OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
            // Write BOM for Excel UTF-8 compatibility
            writer.write('\uFEFF')
            
            // Write summary as comments
            writer.write("# Pyera Finance Export\n")
            writer.write("# Export Date: ${DATE_FORMAT.format(Date())}\n")
            writer.write("# Period: ${DATE_FORMAT.format(Date(summary.startDate))} to ${DATE_FORMAT.format(Date(summary.endDate))}\n")
            writer.write("# Total Income: ${String.format(Locale.getDefault(), "%.2f", summary.totalIncome)}\n")
            writer.write("# Total Expense: ${String.format(Locale.getDefault(), "%.2f", summary.totalExpense)}\n")
            writer.write("# Net Amount: ${String.format(Locale.getDefault(), "%.2f", summary.netAmount)}\n")
            writer.write("# Total Transactions: ${summary.transactionCount}\n")
            writer.write("#\n")
            
            // Write header
            writer.write(CSV_HEADER)
            writer.write("\n")
            
            // Write transaction data
            transactions.sortedByDescending { it.date }.forEach { transaction ->
                val line = buildString {
                    // Date
                    append(escapeCsv(DATE_FORMAT.format(Date(transaction.date))))
                    append(",")
                    
                    // Time
                    append(escapeCsv(TIME_FORMAT.format(Date(transaction.date))))
                    append(",")
                    
                    // Type
                    append(escapeCsv(transaction.type))
                    append(",")
                    
                    // Category
                    append(escapeCsv(categoryNames[transaction.categoryId] ?: "Uncategorized"))
                    append(",")
                    
                    // Amount (negative for expense, positive for income)
                    val signedAmount = if (transaction.type == "EXPENSE") {
                        -transaction.amount
                    } else {
                        transaction.amount
                    }
                    append(String.format(Locale.US, "%.2f", signedAmount))
                    append(",")
                    
                    // Account
                    append(escapeCsv(accountNames[transaction.accountId] ?: "Unknown"))
                    append(",")
                    
                    // Description
                    append(escapeCsv(transaction.note))
                    append(",")
                    
                    // Transaction ID
                    append(transaction.id)
                }
                
                writer.write(line)
                writer.write("\n")
            }
            
            writer.flush()
        }
    }

    /**
     * Escape special CSV characters
     * - Wrap in quotes if contains comma, newline, or quote
     * - Double up quotes within the field
     */
    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\n") || value.contains("\"") || value.contains("\r")
        
        return if (needsQuotes) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
