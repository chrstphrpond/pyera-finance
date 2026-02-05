package com.pyera.app.data.export

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.*
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.repository.ExportSummary
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for exporting transactions to PDF format
 */
class PdfExporter {

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        private val SHORT_DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        // Pyera brand colors (approximated for PDF)
        private val PRIMARY_COLOR = DeviceRgb(212, 255, 0) // NeonYellow
        private val DARK_GREEN = DeviceRgb(10, 14, 13)
        private val SURFACE_DARK = DeviceRgb(26, 31, 29)
        private val TEXT_PRIMARY = DeviceRgb(255, 255, 255)
        private val TEXT_SECONDARY = DeviceRgb(176, 184, 180)
        private val SUCCESS_COLOR = DeviceRgb(76, 175, 80)
        private val ERROR_COLOR = DeviceRgb(255, 82, 82)
    }

    /**
     * Export transactions to PDF
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
        PdfWriter(outputStream).use { writer ->
            PdfDocument(writer).use { pdfDoc ->
                Document(pdfDoc, PageSize.A4).use { document ->
                    document.setMargins(40f, 40f, 60f, 40f)

                    // Add header
                    addHeader(document)

                    // Add summary section
                    addSummarySection(document, summary)

                    // Add transactions table
                    addTransactionsTable(document, transactions, categoryNames, accountNames)

                    // Add footer
                    addFooter(document, pdfDoc)
                }
            }
        }
    }

    private fun addHeader(document: Document) {
        // App name and logo placeholder
        val headerTable = Table(floatArrayOf(1f, 1f)).useAllAvailableWidth()
        
        // Left side - App name
        val appName = Paragraph("Pyera Finance")
            .setFontSize(24f)
            .setBold()
            .setFontColor(PRIMARY_COLOR)
        headerTable.addCell(
            Cell().add(appName)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
        )
        
        // Right side - Export date
        val exportInfo = Paragraph()
            .add(Text("Export Date:\n").setFontColor(TEXT_SECONDARY).setFontSize(10f))
            .add(Text(SHORT_DATE_FORMAT.format(Date())).setFontColor(TEXT_PRIMARY).setFontSize(12f))
        headerTable.addCell(
            Cell().add(exportInfo)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
        )
        
        document.add(headerTable)
        document.add(Paragraph().setMarginBottom(20f))
        
        // Divider line
        val divider = Paragraph()
            .setBorderBottom(SolidBorder(PRIMARY_COLOR, 2f))
            .setMarginBottom(20f)
        document.add(divider)
    }

    private fun addSummarySection(document: Document, summary: ExportSummary) {
        // Summary title
        val summaryTitle = Paragraph("Export Summary")
            .setFontSize(16f)
            .setBold()
            .setFontColor(TEXT_PRIMARY)
            .setMarginBottom(10f)
        document.add(summaryTitle)

        // Create summary table
        val summaryTable = Table(floatArrayOf(1f, 1f)).useAllAvailableWidth()
        summaryTable.setMarginBottom(20f)

        // Period
        addSummaryRow(summaryTable, "Period:", 
            "${SHORT_DATE_FORMAT.format(Date(summary.startDate))} - ${SHORT_DATE_FORMAT.format(Date(summary.endDate))}")
        
        // Total Income
        addSummaryRow(summaryTable, "Total Income:", formatCurrency(summary.totalIncome), SUCCESS_COLOR)
        
        // Total Expense
        addSummaryRow(summaryTable, "Total Expense:", formatCurrency(summary.totalExpense), ERROR_COLOR)
        
        // Net Amount
        val netColor = if (summary.netAmount >= 0) SUCCESS_COLOR else ERROR_COLOR
        addSummaryRow(summaryTable, "Net Amount:", formatCurrency(summary.netAmount), netColor)
        
        // Transaction count
        addSummaryRow(summaryTable, "Total Transactions:", summary.transactionCount.toString())

        document.add(summaryTable)
        
        // Divider
        document.add(Paragraph().setBorderBottom(SolidBorder(ColorConstants.LIGHT_GRAY, 1f)).setMarginBottom(20f))
    }

    private fun addSummaryRow(table: Table, label: String, value: String, valueColor: DeviceRgb? = null) {
        table.addCell(
            Cell().add(Paragraph(label).setFontColor(TEXT_SECONDARY).setFontSize(11f))
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )
        val valueParagraph = Paragraph(value).setFontColor(valueColor ?: TEXT_PRIMARY).setFontSize(11f).setBold()
        table.addCell(
            Cell().add(valueParagraph)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(5f)
        )
    }

    private fun addTransactionsTable(
        document: Document,
        transactions: List<TransactionEntity>,
        categoryNames: Map<Int, String>,
        accountNames: Map<Long, String>
    ) {
        // Transactions title
        val transactionsTitle = Paragraph("Transactions")
            .setFontSize(16f)
            .setBold()
            .setFontColor(TEXT_PRIMARY)
            .setMarginBottom(10f)
        document.add(transactionsTitle)

        // Create transactions table
        val columnWidths = floatArrayOf(2f, 1f, 1.5f, 1f, 1.5f, 2f)
        val table = Table(columnWidths).useAllAvailableWidth()
        
        // Header row
        val headers = listOf("Date", "Type", "Category", "Amount", "Account", "Description")
        headers.forEach { header ->
            table.addHeaderCell(
                Cell().add(Paragraph(header).setBold().setFontColor(TEXT_PRIMARY).setFontSize(10f))
                    .setBackgroundColor(SURFACE_DARK)
                    .setBorder(SolidBorder(ColorConstants.DARK_GRAY, 1f))
                    .setPadding(8f)
            )
        }

        // Data rows
        transactions.sortedByDescending { it.date }.forEach { transaction ->
            val isIncome = transaction.type == "INCOME"
            val amountColor = if (isIncome) SUCCESS_COLOR else ERROR_COLOR
            val amountPrefix = if (isIncome) "+" else "-"

            // Date
            table.addCell(createDataCell(DATE_FORMAT.format(Date(transaction.date))))
            
            // Type
            table.addCell(createDataCell(transaction.type, 
                if (isIncome) SUCCESS_COLOR else ERROR_COLOR))
            
            // Category
            table.addCell(createDataCell(categoryNames[transaction.categoryId] ?: "Uncategorized"))
            
            // Amount
            table.addCell(
                createDataCell("$amountPrefix${formatCurrency(transaction.amount)}", amountColor)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
            
            // Account
            table.addCell(createDataCell(accountNames[transaction.accountId] ?: "Unknown"))
            
            // Description
            table.addCell(createDataCell(transaction.note.ifEmpty { "-" }))
        }

        document.add(table)
    }

    private fun createDataCell(text: String, color: DeviceRgb = TEXT_PRIMARY): Cell {
        return Cell().add(Paragraph(text).setFontColor(color).setFontSize(9f))
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
            .setPadding(6f)
    }

    private fun addFooter(document: Document, pdfDoc: PdfDocument) {
        val pageCount = pdfDoc.numberOfPages
        
        for (i in 1..pageCount) {
            val page = pdfDoc.getPage(i)
            val footerTable = Table(floatArrayOf(1f, 1f)).useAllAvailableWidth()
            
            // Left - Generated by
            footerTable.addCell(
                Cell().add(Paragraph("Generated by Pyera Finance")
                    .setFontColor(TEXT_SECONDARY)
                    .setFontSize(8f))
                    .setBorder(Border.NO_BORDER)
            )
            
            // Right - Page number
            footerTable.addCell(
                Cell().add(Paragraph("Page $i of $pageCount")
                    .setFontColor(TEXT_SECONDARY)
                    .setFontSize(8f))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
            
            footerTable.setFixedPosition(i, 40f, 30f, page.pageSize.width - 80f)
            document.add(footerTable)
        }
    }

    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "₱%,.2f", amount)
    }
}
