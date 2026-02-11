package com.pyera.app.domain.ocr

import com.google.mlkit.vision.text.Text

/**
 * Parser for extracting receipt data from OCR text.
 * Heavy regex processing should be called from IO dispatcher.
 */
class ReceiptParser {

    data class ReceiptData(
        val merchant: String? = null,
        val date: Long? = null,
        val totalAmount: Double? = null
    )

    /**
     * Parse OCR text to extract receipt data.
     * Note: This method performs CPU-intensive regex operations.
     * It should be called from a background thread (e.g., IO dispatcher).
     * 
     * @param text The OCR text from ML Kit
     * @return ReceiptData containing extracted information
     */
    fun parse(text: Text): ReceiptData {
        val blocks = text.textBlocks
        var merchant: String? = null
        
        // Simple heuristic: First non-empty block is often the merchant
        if (blocks.isNotEmpty()) {
            merchant = blocks[0].text.lines().firstOrNull { it.isNotBlank() }
        }

        val fullText = text.text
        val totalAmount = findTotalAmount(fullText)

        return ReceiptData(
            merchant = merchant,
            totalAmount = totalAmount,
            date = System.currentTimeMillis() // Placeholder: Date parsing is complex, defaulting to now
        )
    }

    /**
     * Find the total amount from receipt text.
     * Searches for lines containing "total" and extracts the largest numeric value.
     * 
     * @param text The full text from OCR
     * @return The largest amount found, or null if none found
     */
    private fun findTotalAmount(text: String): Double? {
        // Regex to find currency-like patterns (e.g., 1,234.56 or 1234.56)
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
