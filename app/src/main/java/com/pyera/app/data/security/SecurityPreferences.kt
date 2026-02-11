package com.pyera.app.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted SharedPreferences for storing sensitive security settings.
 * Uses AES256_GCM for encryption to ensure PIN codes and biometric settings are secure.
 */
@Singleton
class SecurityPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILE_NAME = "security_prefs"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_PIN_CODE = "pin_code"
        private const val KEY_USE_BIOMETRIC = "use_biometric"
        private const val KEY_LOCK_TIMEOUT = "lock_timeout"
        private const val KEY_LAST_ACTIVE_TIME = "last_active_time"
        private const val DEFAULT_LOCK_TIMEOUT = 300000L // 5 minutes in milliseconds
        private const val PIN_LENGTH_MIN = 4
        private const val PIN_LENGTH_MAX = 6
        private const val PIN_HASH_PREFIX = "v1"
        private const val PIN_HASH_DELIMITER = ":"
        private const val PIN_SALT_BYTES = 16
        private const val PIN_HASH_BITS = 256
        private const val PIN_HASH_ITERATIONS = 120_000
        
        // PIN rate limiting constants
        private const val KEY_FAILED_ATTEMPTS = "failed_pin_attempts"
        private const val KEY_LOCKOUT_END_TIME = "pin_lockout_end_time"
        private const val MAX_ATTEMPTS = 5
        private const val BASE_LOCKOUT_MS = 30000L // 30 seconds
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val secureRandom: SecureRandom by lazy { SecureRandom() }

    /**
     * Whether app lock is enabled
     */
    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, value).apply()

    /**
     * The stored PIN hash (salted PBKDF2, encrypted at rest by EncryptedSharedPreferences)
     */
    var pinCode: String?
        get() = prefs.getString(KEY_PIN_CODE, null)
        set(value) {
            val editor = prefs.edit()
            if (value.isNullOrBlank()) {
                editor.remove(KEY_PIN_CODE).apply()
                return
            }

            val storedValue = if (value.startsWith("$PIN_HASH_PREFIX$PIN_HASH_DELIMITER")) {
                value
            } else {
                hashPin(value)
            }
            editor.putString(KEY_PIN_CODE, storedValue).apply()
        }

    /**
     * Whether biometric authentication is enabled
     */
    var useBiometric: Boolean
        get() = prefs.getBoolean(KEY_USE_BIOMETRIC, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_BIOMETRIC, value).apply()

    /**
     * Lock timeout in milliseconds (default: 5 minutes)
     */
    var lockTimeout: Long
        get() = prefs.getLong(KEY_LOCK_TIMEOUT, DEFAULT_LOCK_TIMEOUT)
        set(value) = prefs.edit().putLong(KEY_LOCK_TIMEOUT, value).apply()

    /**
     * Last time the app was active (used for timeout calculation)
     */
    var lastActiveTime: Long
        get() = prefs.getLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis())
        set(value) = prefs.edit().putLong(KEY_LAST_ACTIVE_TIME, value).apply()

    /**
     * Check if PIN is set
     */
    fun isPinSet(): Boolean = !pinCode.isNullOrBlank()

    /**
     * Validate PIN format (4-6 digits)
     */
    fun isValidPinFormat(pin: String): Boolean {
        return pin.length in PIN_LENGTH_MIN..PIN_LENGTH_MAX && pin.all { it.isDigit() }
    }

    /**
     * Verify if the provided PIN matches the stored PIN
     */
    fun verifyPin(pin: String): Boolean {
        val stored = pinCode ?: return false

        return if (stored.startsWith("$PIN_HASH_PREFIX$PIN_HASH_DELIMITER")) {
            verifyHashedPin(pin, stored)
        } else if (isLegacyPinFormat(stored)) {
            val matches = pin == stored
            if (matches) {
                // Upgrade legacy plaintext PIN to hashed format.
                pinCode = pin
            }
            matches
        } else {
            false
        }
    }

    /**
     * Clear all security settings (used when disabling app lock)
     */
    fun clearAll() {
        prefs.edit().apply {
            remove(KEY_APP_LOCK_ENABLED)
            remove(KEY_PIN_CODE)
            remove(KEY_USE_BIOMETRIC)
            remove(KEY_LOCK_TIMEOUT)
            remove(KEY_LAST_ACTIVE_TIME)
            remove(KEY_FAILED_ATTEMPTS)
            remove(KEY_LOCKOUT_END_TIME)
            apply()
        }
    }

    /**
     * Record a failed PIN attempt and calculate lockout
     * Returns the remaining lockout time in milliseconds
     */
    fun recordFailedAttempt(): Long {
        val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        
        return if (attempts >= MAX_ATTEMPTS) {
            // Calculate exponential backoff: 30s, 60s, 120s, 240s, 480s, etc.
            val consecutiveFailures = (attempts - MAX_ATTEMPTS) + 1
            val lockoutDuration = BASE_LOCKOUT_MS * (1L shl (consecutiveFailures - 1))
            val lockoutEndTime = System.currentTimeMillis() + lockoutDuration
            
            prefs.edit().apply {
                putInt(KEY_FAILED_ATTEMPTS, attempts)
                putLong(KEY_LOCKOUT_END_TIME, lockoutEndTime)
                apply()
            }
            lockoutDuration
        } else {
            prefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts).apply()
            0L
        }
    }

    /**
     * Check if the user is currently locked out from PIN entry
     */
    fun isLockedOut(): Boolean {
        val lockoutEndTime = prefs.getLong(KEY_LOCKOUT_END_TIME, 0)
        return lockoutEndTime > System.currentTimeMillis()
    }

    /**
     * Get the remaining lockout time in milliseconds
     * Returns 0 if not locked out
     */
    fun getRemainingLockoutTime(): Long {
        val lockoutEndTime = prefs.getLong(KEY_LOCKOUT_END_TIME, 0)
        val remaining = lockoutEndTime - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0L
    }

    /**
     * Get the number of remaining allowed attempts before lockout
     */
    fun getRemainingAttempts(): Int {
        if (isLockedOut()) return 0
        val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        return (MAX_ATTEMPTS - attempts).coerceAtLeast(0)
    }

    /**
     * Reset failed attempts counter (called on successful authentication)
     */
    fun resetAttempts() {
        prefs.edit().apply {
            remove(KEY_FAILED_ATTEMPTS)
            remove(KEY_LOCKOUT_END_TIME)
            apply()
        }
    }

    /**
     * Get available timeout options
     */
    fun getTimeoutOptions(): List<TimeoutOption> = listOf(
        TimeoutOption(30000L, "30 seconds"),
        TimeoutOption(60000L, "1 minute"),
        TimeoutOption(300000L, "5 minutes"),
        TimeoutOption(600000L, "10 minutes"),
        TimeoutOption(900000L, "15 minutes"),
        TimeoutOption(1800000L, "30 minutes")
    )

    data class TimeoutOption(
        val millis: Long,
        val displayText: String
    )

    private fun isLegacyPinFormat(value: String): Boolean {
        return value.length in PIN_LENGTH_MIN..PIN_LENGTH_MAX && value.all { it.isDigit() }
    }

    private fun hashPin(pin: String): String {
        val salt = ByteArray(PIN_SALT_BYTES)
        secureRandom.nextBytes(salt)
        val hash = pbkdf2(pin, salt, PIN_HASH_ITERATIONS, PIN_HASH_BITS)

        val saltEncoded = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashEncoded = Base64.encodeToString(hash, Base64.NO_WRAP)

        return listOf(
            PIN_HASH_PREFIX,
            PIN_HASH_ITERATIONS.toString(),
            saltEncoded,
            hashEncoded
        ).joinToString(PIN_HASH_DELIMITER)
    }

    private fun verifyHashedPin(pin: String, stored: String): Boolean {
        val parts = stored.split(PIN_HASH_DELIMITER)
        if (parts.size != 4) return false
        if (parts[0] != PIN_HASH_PREFIX) return false

        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = try {
            Base64.decode(parts[2], Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            return false
        }
        val expectedHash = try {
            Base64.decode(parts[3], Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            return false
        }

        val actualHash = pbkdf2(pin, salt, iterations, expectedHash.size * 8)
        return constantTimeEquals(expectedHash, actualHash)
    }

    private fun pbkdf2(pin: String, salt: ByteArray, iterations: Int, keyLengthBits: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, keyLengthBits)
        val algorithms = listOf("PBKDF2WithHmacSHA256", "PBKDF2WithHmacSHA1")
        var lastError: Exception? = null
        for (algorithm in algorithms) {
            try {
                val factory = SecretKeyFactory.getInstance(algorithm)
                return factory.generateSecret(spec).encoded
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw IllegalStateException("PBKDF2 algorithm not available", lastError)
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
