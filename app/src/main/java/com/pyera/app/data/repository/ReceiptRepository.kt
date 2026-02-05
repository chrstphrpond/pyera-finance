package com.pyera.app.data.repository

import android.net.Uri

/**
 * Repository interface for receipt management.
 */
interface ReceiptRepository {
    /**
     * Save a receipt image to local storage.
     * @param transactionId The transaction ID
     * @param imageUri The source image URI
     * @return Result containing the local file path on success
     */
    suspend fun saveReceipt(transactionId: Long, imageUri: Uri): Result<String>

    /**
     * Upload a receipt to cloud storage.
     * @param transactionId The transaction ID
     * @param localPath The local file path
     * @return Result containing the cloud URL on success
     */
    suspend fun uploadReceiptToCloud(transactionId: Long, localPath: String): Result<String>

    /**
     * Get the local receipt path for a transaction.
     * @param transactionId The transaction ID
     * @return The local file path or null if not found
     */
    suspend fun getReceiptPath(transactionId: Long): String?

    /**
     * Delete a receipt (both local and cloud).
     * @param transactionId The transaction ID
     * @param cloudUrl Optional cloud URL to delete from cloud storage
     * @return Result indicating success or failure
     */
    suspend fun deleteReceipt(transactionId: Long, cloudUrl: String? = null): Result<Unit>

    /**
     * Compress an image to reduce file size.
     * @param uri The image URI to compress
     * @return URI of the compressed image
     */
    suspend fun compressImage(uri: Uri): Uri

    /**
     * Process and save a receipt from camera or gallery.
     * This includes resizing, compression, and saving to app storage.
     * @param transactionId The transaction ID
     * @param sourceUri The source image URI
     * @return Result containing the local file path on success
     */
    suspend fun processAndSaveReceipt(transactionId: Long, sourceUri: Uri): Result<String>

    /**
     * Sync a receipt to cloud storage.
     * Uploads the local receipt and returns the cloud URL.
     * @param transactionId The transaction ID
     * @param localPath The local file path
     * @return Result containing the cloud URL on success
     */
    suspend fun syncReceiptToCloud(transactionId: Long, localPath: String): Result<String>
}
