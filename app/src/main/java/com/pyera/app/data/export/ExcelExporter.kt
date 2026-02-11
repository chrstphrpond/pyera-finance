package com.pyera.app.data.export

import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.repository.ExportSummary
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for exporting transactions to Excel format
 */
class ExcelExporter {

    companion object {
        private const val SHEET_TRANSACTIONS = "Transactions"
        private const val SHEET_SUMMARY = "Summary"
        private const val SHEET_CATEGORIES = "By Category"
        
        private val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        private val SHORT_DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    /**
     * Export transactions to Excel workbook
     * @param transactions List of transactions to export
     * @param outputStream Output stream to write to
     * @param summary Export summary statistics
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
        XSSFWorkbook().use { workbook ->
            // Create cell styles
            val headerStyle = createHeaderStyle(workbook)
            val currencyStyle = createCurrencyStyle(workbook)
            val dateStyle = createDateStyle(workbook)
            val dataStyle = createDataStyle(workbook)
            val summaryLabelStyle = createSummaryLabelStyle(workbook)
            val summaryValueStyle = createSummaryValueStyle(workbook)

            // Create Transactions sheet
            createTransactionsSheet(
                workbook, transactions, categoryNames, accountNames,
                headerStyle, currencyStyle, dateStyle, dataStyle
            )

            // Create Summary sheet
            createSummarySheet(
                workbook, summary, headerStyle, summaryLabelStyle, summaryValueStyle, currencyStyle
            )

            // Create Category breakdown sheet
            createCategorySheet(
                workbook, transactions, categoryNames,
                headerStyle, currencyStyle, dataStyle
            )

            // Write to output stream
            workbook.write(outputStream)
        }
    }

    private fun createTransactionsSheet(
        workbook: Workbook,
        transactions: List<TransactionEntity>,
        categoryNames: Map<Int, String>,
        accountNames: Map<Long, String>,
        headerStyle: CellStyle,
        currencyStyle: CellStyle,
        dateStyle: CellStyle,
        dataStyle: CellStyle
    ) {
        val sheet = workbook.createSheet(SHEET_TRANSACTIONS)
        
        // Create header row
        val headerRow = sheet.createRow(0)
        val headers = listOf("Date", "Type", "Category", "Amount", "Account", "Description")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }

        // Add transaction data
        transactions.sortedByDescending { it.date }.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            
            // Date
            row.createCell(0).apply {
                setCellValue(Date(transaction.date))
                cellStyle = dateStyle
            }
            
            // Type
            row.createCell(1).apply {
                setCellValue(transaction.type)
                cellStyle = dataStyle
            }
            
            // Category
            row.createCell(2).apply {
                setCellValue(categoryNames[transaction.categoryId] ?: "Uncategorized")
                cellStyle = dataStyle
            }
            
            // Amount
            row.createCell(3).apply {
                setCellValue(transaction.amount)
                cellStyle = currencyStyle
            }
            
            // Account
            row.createCell(4).apply {
                setCellValue(accountNames[transaction.accountId] ?: "Unknown")
                cellStyle = dataStyle
            }
            
            // Description
            row.createCell(5).apply {
                setCellValue(transaction.note)
                cellStyle = dataStyle
            }
        }

        // Auto-size columns
        headers.indices.forEach { sheet.autoSizeColumn(it) }
    }

    private fun createSummarySheet(
        workbook: Workbook,
        summary: ExportSummary,
        headerStyle: CellStyle,
        labelStyle: CellStyle,
        valueStyle: CellStyle,
        currencyStyle: CellStyle
    ) {
        val sheet = workbook.createSheet(SHEET_SUMMARY)
        
        // Title
        val titleRow = sheet.createRow(0)
        titleRow.createCell(0).apply {
            setCellValue("Pyera Finance Export Summary")
            cellStyle = headerStyle
        }
        
        // Export period
        var currentRow = 2
        sheet.createRow(currentRow++).apply {
            createCell(0).apply {
                setCellValue("Export Period")
                cellStyle = labelStyle
            }
        }
        
        sheet.createRow(currentRow++).apply {
            createCell(0).setCellValue("From:")
            createCell(1).setCellValue(SHORT_DATE_FORMAT.format(Date(summary.startDate)))
        }
        
        sheet.createRow(currentRow++).apply {
            createCell(0).setCellValue("To:")
            createCell(1).setCellValue(SHORT_DATE_FORMAT.format(Date(summary.endDate)))
        }
        
        // Financial summary
        currentRow += 2
        sheet.createRow(currentRow++).apply {
            createCell(0).apply {
                setCellValue("Financial Summary")
                cellStyle = labelStyle
            }
        }
        
        sheet.createRow(currentRow++).apply {
            createCell(0).setCellValue("Total Income:")
            createCell(1).apply {
                setCellValue(summary.totalIncome)
                cellStyle = currencyStyle
            }
        }
        
        sheet.createRow(currentRow++).apply {
            createCell(0).setCellValue("Total Expense:")
            createCell(1).apply {
                setCellValue(summary.totalExpense)
                cellStyle = currencyStyle
            }
        }
        
        sheet.createRow(currentRow++).apply {
            createCell(0).setCellValue("Net Amount:")
            createCell(1).apply {
                setCellValue(summary.netAmount)
                cellStyle = currencyStyle
            }
        }
        
        sheet.createRow(currentRow).apply {
            createCell(0).setCellValue("Total Transactions:")
            createCell(1).apply {
                setCellValue(summary.transactionCount.toDouble())
                cellStyle = valueStyle
            }
        }
        
        // Auto-size columns
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
    }

    private fun createCategorySheet(
        workbook: Workbook,
        transactions: List<TransactionEntity>,
        categoryNames: Map<Int, String>,
        headerStyle: CellStyle,
        currencyStyle: CellStyle,
        dataStyle: CellStyle
    ) {
        val sheet = workbook.createSheet(SHEET_CATEGORIES)
        
        // Group transactions by category
        val categoryBreakdown = transactions
            .filter { it.categoryId != null }
            .groupBy { it.categoryId }
            .mapValues { entry ->
                val income = entry.value.filter { it.type == "INCOME" }.sumOf { it.amount }
                val expense = entry.value.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                Triple(categoryNames[entry.key] ?: "Unknown", income, expense)
            }
            .values
            .sortedByDescending { it.second + it.third }

        // Header row
        val headerRow = sheet.createRow(0)
        listOf("Category", "Income", "Expense", "Net").forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }

        // Category data
        categoryBreakdown.forEachIndexed { index, (name, income, expense) ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).apply {
                setCellValue(name)
                cellStyle = dataStyle
            }
            row.createCell(1).apply {
                setCellValue(income)
                cellStyle = currencyStyle
            }
            row.createCell(2).apply {
                setCellValue(expense)
                cellStyle = currencyStyle
            }
            row.createCell(3).apply {
                setCellValue(income - expense)
                cellStyle = currencyStyle
            }
        }

        // Auto-size columns
        (0..3).forEach { sheet.autoSizeColumn(it) }
    }

    // Cell styles
    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
            })
            fillForegroundColor = IndexedColors.GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
        }
    }

    private fun createCurrencyStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            dataFormat = workbook.createDataFormat().getFormat("₱#,##0.00")
            alignment = HorizontalAlignment.RIGHT
        }
    }

    private fun createDateStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            dataFormat = workbook.createDataFormat().getFormat("MMM dd, yyyy HH:mm")
            alignment = HorizontalAlignment.LEFT
        }
    }

    private fun createDataStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
        }
    }

    private fun createSummaryLabelStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 12
            })
        }
    }

    private fun createSummaryValueStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply {
                bold = true
            })
            alignment = HorizontalAlignment.RIGHT
        }
    }
}
