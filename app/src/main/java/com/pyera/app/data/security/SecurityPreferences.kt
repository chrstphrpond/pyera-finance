package com.pyera.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
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

    /**
     * Whether app lock is enabled
     */
    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, value).apply()

    /**
     * The stored PIN code (hashed/encrypted by EncryptedSharedPreferences)
     */
    var pinCode: String?
        get() = prefs.getString(KEY_PIN_CODE, null)
        set(value) = prefs.edit().putString(KEY_PIN_CODE, value).apply()

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
        return pin == pinCode
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
}
