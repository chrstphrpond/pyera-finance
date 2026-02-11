package com.pyera.app.domain.repository

import android.net.Uri
import com.pyera.app.domain.ocr.ReceiptParser

interface OcrRepository {
    suspend fun processReceipt(imageUri: Uri): ReceiptParser.ReceiptData
}
