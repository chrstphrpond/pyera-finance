package com.pyera.app.domain.ocr

/**
 * Exception thrown when OCR processing fails.
 */
class OcrProcessingException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
