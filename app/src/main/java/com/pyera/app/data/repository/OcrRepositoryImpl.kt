package com.pyera.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pyera.app.domain.ocr.OcrProcessingException
import com.pyera.app.domain.ocr.ReceiptParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OcrRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val receiptParser: ReceiptParser
) : OcrRepository {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun processReceipt(imageUri: Uri): ReceiptParser.ReceiptData {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val visionText = recognizer.process(image).await()
            receiptParser.parse(visionText)
        } catch (e: Exception) {
            // Log error and rethrow or return Result.failure()
            throw OcrProcessingException("Failed to process receipt", e)
        }
    }
}
