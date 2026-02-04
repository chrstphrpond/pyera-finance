package com.pyera.app.domain.ocr

import com.google.mlkit.vision.text.Text
import java.util.regex.Pattern

class ReceiptParser {

    data class ReceiptData(
        val merchant: String? = null,
        val date: Long? = null,
        val totalAmount: Double? = null
    )

    fun parse(text: Text): ReceiptData {
        val blocks = text.textBlocks
        var merchant: String? = null
        var totalAmount: Double? = null
        
        // Simple heuristic: First non-empty block is often the merchant
        if (blocks.isNotEmpty()) {
            merchant = blocks[0].text.lines().firstOrNull { it.isNotBlank() }
        }

        val fullText = text.text
        totalAmount = findTotalAmount(fullText)

        return ReceiptData(
            merchant = merchant,
            totalAmount = totalAmount,
            date = System.currentTimeMillis() // Placeholder: Date parsing is complex, defaulting to now
        )
    }

    private fun findTotalAmount(text: String): Double? {
        // Regex to find currency-like patterns (e.g., 1,234.56 or 1234.56)
        // This is a naive implementation.
        val lines = text.lines()
        var maxAmount = 0.0
        
        // Look for lines containing "Total" (case insensitive)
        val totalLines = lines.filter { it.contains("total", ignoreCase = true) }
        
        val amountRegex = Regex("\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?")
        
        val linesToSearch = if (totalLines.isNotEmpty()) totalLines else lines

        for (line in linesToSearch) {
             val matches = amountRegex.findAll(line)
             for (match in matches) {
                 val valueStr = match.value.replace(",", "")
                 val value = valueStr.toDoubleOrNull()
                 if (value != null && value > maxAmount) {
                     maxAmount = value
                 }
             }
        }
        
        return if (maxAmount > 0) maxAmount else null
    }
}
