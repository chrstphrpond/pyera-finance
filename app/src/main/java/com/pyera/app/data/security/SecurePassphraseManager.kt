package com.pyera.app.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

class SecurePassphraseManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "secure_passphrase_prefs"
        private const val PASSPHRASE_KEY = "database_passphrase"
        private const val PASSPHRASE_BYTES = 32
    }

    private val secureRandom: SecureRandom by lazy { SecureRandom() }
    
    fun getOrCreatePassphrase(): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        val savedPassphrase = prefs.getString(PASSPHRASE_KEY, null)

        return if (savedPassphrase != null) {
            // Prefer Base64-encoded values; fall back to legacy UTF-8 string.
            try {
                Base64.decode(savedPassphrase, Base64.NO_WRAP)
            } catch (e: IllegalArgumentException) {
                val legacy = savedPassphrase.toByteArray(Charsets.UTF_8)
                prefs.edit()
                    .putString(PASSPHRASE_KEY, Base64.encodeToString(legacy, Base64.NO_WRAP))
                    .apply()
                legacy
            }
        } else {
            // Generate new passphrase
            val newPassphrase = generateSecurePassphrase()
            val encoded = Base64.encodeToString(newPassphrase, Base64.NO_WRAP)
            prefs.edit().putString(PASSPHRASE_KEY, encoded).apply()
            newPassphrase
        }
    }
    
    private fun generateSecurePassphrase(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_BYTES)
        secureRandom.nextBytes(passphrase)
        return passphrase
    }
}
