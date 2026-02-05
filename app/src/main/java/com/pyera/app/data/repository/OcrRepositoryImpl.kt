package com.pyera.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pyera.app.domain.ocr.OcrProcessingException
import com.pyera.app.domain.ocr.ReceiptParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * OCR Repository implementation for processing receipt images.
 * All heavy processing is done on IO dispatcher to avoid blocking the main thread.
 */
class OcrRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val receiptParser: ReceiptParser
) : OcrRepository {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Process a receipt image and extract relevant data.
     * This operation is performed on the IO dispatcher to prevent UI blocking.
     * 
     * @param imageUri URI of the receipt image to process
     * @return ReceiptData containing merchant, date, and total amount
     * @throws OcrProcessingException if processing fails
     */
    override suspend fun processReceipt(imageUri: Uri): ReceiptParser.ReceiptData {
        return try {
            // Image loading and ML Kit processing
            val image = withContext(Dispatchers.IO) {
                InputImage.fromFilePath(context, imageUri)
            }
            
            // ML Kit processing (runs on its own thread pool via await())
            val visionText = recognizer.process(image).await()
            
            // Parsing is CPU intensive - move to IO dispatcher
            withContext(Dispatchers.IO) {
                receiptParser.parse(visionText)
            }
        } catch (e: Exception) {
            // Log error and rethrow
            throw OcrProcessingException("Failed to process receipt", e)
        }
    }
}
