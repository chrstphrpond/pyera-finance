package com.pyera.app.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePassphraseManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "secure_passphrase_prefs"
        private const val PASSPHRASE_KEY = "database_passphrase"
    }
    
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
            savedPassphrase.toByteArray(Charsets.UTF_8)
        } else {
            // Generate new passphrase
            val newPassphrase = generateSecurePassphrase()
            prefs.edit().putString(PASSPHRASE_KEY, String(newPassphrase, Charsets.UTF_8)).apply()
            newPassphrase
        }
    }
    
    private fun generateSecurePassphrase(): ByteArray {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..32)
            .map { charset.random() }
            .joinToString("")
            .toByteArray(Charsets.UTF_8)
    }
}
