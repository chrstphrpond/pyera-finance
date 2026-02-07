package com.pyera.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun login(email: String, pass: String): Result<FirebaseUser>
    suspend fun register(email: String, pass: String, name: String): Result<FirebaseUser>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>
    fun logout()
    
    // Biometric authentication methods
    fun isBiometricEnabled(): Boolean
    fun setBiometricEnabled(enabled: Boolean)
    fun getStoredEmail(): String?
    fun storeCredentials(email: String): Result<Unit>
    fun clearStoredCredentials()
    fun hasStoredCredentials(): Boolean
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : AuthRepository {
    
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val ENCRYPTED_PREFS_NAME = "encrypted_auth_prefs"
        private const val KEY_STORED_EMAIL = "stored_email"
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        createEncryptedSharedPreferences()
    }
    
    private fun createEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun login(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, pass: String, name: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            result.user?.let { user ->
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates).await()
                } catch (_: Exception) {
                    // Best-effort profile update; registration should still succeed.
                }
                Result.success(user)
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Google sign-in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        auth.signOut()
        // Note: We don't clear stored credentials on logout
        // so biometric login can still work next time
    }
    
    // Biometric Authentication Implementation
    
    override fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    override fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
        }
    }
    
    override fun getStoredEmail(): String? {
        return try {
            encryptedPrefs.getString(KEY_STORED_EMAIL, null)
        } catch (e: Exception) {
            // If decryption fails, clear and return null
            clearStoredCredentials()
            null
        }
    }
    
    override fun storeCredentials(email: String): Result<Unit> {
        return try {
            encryptedPrefs.edit {
                putString(KEY_STORED_EMAIL, email)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // If encryption fails, clear any partial data and return failure
            clearStoredCredentials()
            Result.failure(e)
        }
    }
    
    override fun clearStoredCredentials() {
        encryptedPrefs.edit {
            remove(KEY_STORED_EMAIL)
        }
    }
    
    override fun hasStoredCredentials(): Boolean {
        return auth.currentUser != null
    }
}
