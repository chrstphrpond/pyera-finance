# Pyera Finance - Security Implementation Plan

**Date:** February 5, 2026  
**Based on:** Security Review Report  
**Priority:** Critical - Immediate Action Required

---

## Overview

This plan addresses critical security vulnerabilities identified in the Pyera Finance Android app. The implementation is divided into phases based on severity levels.

---

## Phase 1: Critical Security Fixes (Immediate - Within 24 Hours)

### Task 1.1: Rotate Exposed Kimi API Key
**Priority:** 🔴 CRITICAL  
**Assignee:** DevOps/Security Lead

**Steps:**
1. Generate a new Kimi API key from Moonshot AI Console
2. Revoke the old exposed key: `sk-kimi-eo71AzD4aHGSIzVny740hvWJloIT0a36Q384X5uusjWsstb1wqO6PBeFOazSbAPQ`
3. Update the key in your local environment (not in code)
4. Verify `local.properties` is in `.gitignore`

**Verification:**
- [ ] Old key revoked in Moonshot console
- [ ] New key generated and working
- [ ] Git history checked for key exposure

---

### Task 1.2: Restrict Firebase API Keys
**Priority:** 🔴 CRITICAL  
**Assignee:** Firebase Admin

**Steps:**
1. Go to Google Cloud Console → APIs & Services → Credentials
2. Find the Firebase API key used in `google-services.json`
3. Add application restrictions:
   - Android apps
   - Package name: `com.pyera.app`
   - SHA-1 certificate fingerprint (debug and release)
4. Add API restrictions:
   - Firebase Installations API
   - Firebase Cloud Messaging API (if used)
   - Identity Toolkit API

**Verification:**
- [ ] API key restricted to specific app signature
- [ ] API key has limited API access
- [ ] Test build still works

---

### Task 1.3: Add Certificate Pinning
**Priority:** 🟠 HIGH  
**File:** `app/src/main/java/com/pyera/app/data/repository/ChatRepositoryImpl.kt`

**Implementation:**

```kotlin
import okhttp3.CertificatePinner

@Singleton
class ChatRepositoryImpl @Inject constructor() : ChatRepository {

    private val apiKey = BuildConfig.KIMI_API_KEY
    private val baseUrl = "https://api.moonshot.cn/v1/chat/completions"
    
    // Certificate pinning for api.moonshot.cn
    private val certificatePinner = CertificatePinner.Builder()
        .add("api.moonshot.cn", "sha256/PLACEHOLDER_GET_ACTUAL_PIN_FROM_SERVER") // TODO: Replace with actual pin
        .build()
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .certificatePinner(certificatePinner)
        .build()
    // ... rest of class
}
```

**Note:** Get the actual SHA-256 pin by running:
```bash
openssl s_client -servername api.moonshot.cn -connect api.moonshot.cn:443 < /dev/null 2>/dev/null | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
```

**Verification:**
- [ ] Pin generated and added
- [ ] API calls still work
- [ ] MITM test with Charles Proxy fails (expected)

---

## Phase 2: High Priority Security Features (Within 1 Week)

### Task 2.1: Implement Root Detection
**Priority:** 🟠 HIGH  
**New File:** `app/src/main/java/com/pyera/app/security/SecurityChecker.kt`

**Implementation:**

```kotlin
package com.pyera.app.security

import android.content.Context
import android.os.Build
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityChecker @Inject constructor(
    private val context: Context
) {
    
    fun isDeviceSecure(): Boolean {
        return !isRooted() && !isEmulator()
    }
    
    fun getSecurityIssues(): List<String> {
        val issues = mutableListOf<String>()
        if (isRooted()) issues.add("Device appears to be rooted")
        if (isEmulator()) issues.add("App is running on emulator")
        return issues
    }
    
    private fun isRooted(): Boolean {
        val testPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        // Check for superuser files
        if (testPaths.any { File(it).exists() }) return true
        
        // Check for busybox
        if (File("/system/xbin/busybox").exists()) return true
        
        // Check for test-keys build tag
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true
        
        // Try to execute su command
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT?.startsWith("generic") == true ||
                Build.FINGERPRINT?.startsWith("unknown") == true ||
                Build.MODEL?.contains("google_sdk") == true ||
                Build.MODEL?.contains("Emulator") == true ||
                Build.MODEL?.contains("Android SDK built for x86") == true ||
                Build.BOARD?.lowercase()?.contains("nox") == true ||
                Build.BOOTLOADER?.lowercase()?.contains("nox") == true ||
                Build.HARDWARE == "goldfish" ||
                Build.HARDWARE == "ranchu" ||
                Build.HARDWARE?.lowercase()?.contains("nox") == true ||
                Build.PRODUCT?.contains("sdk") == true ||
                Build.PRODUCT?.contains("nox") == true)
    }
}
```

**Usage in MainActivity.kt:**

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var securityChecker: SecurityChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check device security before proceeding
        if (!securityChecker.isDeviceSecure()) {
            val issues = securityChecker.getSecurityIssues()
            Log.w("Security", "Security issues detected: $issues")
            // Option 1: Show warning dialog
            // Option 2: Block app usage for high-security features
            // Option 3: Disable sensitive features
        }
        
        // ... rest of onCreate
    }
}
```

**Add to AppModule.kt:**

```kotlin
@Provides
@Singleton
fun provideSecurityChecker(@ApplicationContext context: Context): SecurityChecker = 
    SecurityChecker(context)
```

**Verification:**
- [ ] SecurityChecker class created
- [ ] Root detection works on rooted device
- [ ] Emulator detection works
- [ ] App behaves correctly on secure devices

---

### Task 2.2: Add Screenshot/Screen Recording Protection
**Priority:** 🟠 HIGH  
**File:** `app/src/main/java/com/pyera/app/MainActivity.kt`

**Implementation:**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Prevent screenshots and screen recording
    window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )
    
    // ... rest of onCreate
}
```

**Add import:**
```kotlin
import android.view.WindowManager
```

**Verification:**
- [ ] Screenshot attempt shows black screen
- [ ] Screen recording shows black screen
- [ ] App functionality unaffected

---

### Task 2.3: Remove Sensitive Data from Logs
**Priority:** 🟠 HIGH  
**Files to Update:**
- `app/src/main/java/com/pyera/app/data/repository/GoogleAuthHelper.kt`
- `app/src/main/java/com/pyera/app/ui/auth/LoginScreen.kt`

**Changes for GoogleAuthHelper.kt:**

```kotlin
// REMOVE or comment out these lines:
// Line 32: Log.d(TAG, "Initializing Google Sign-In with Web Client ID: $webClientId")
// Line 75: Log.d(TAG, "Account retrieved: ${account?.displayName}, email: ${account?.email}")

// Replace with:
Log.d(TAG, "Initializing Google Sign-In...")
Log.d(TAG, "Account retrieved successfully")
```

**Changes for LoginScreen.kt:**

```kotlin
// Review lines 108-124
// Remove any logging of:
// - ID tokens
// - User emails
// - Account details
// Only log generic success/failure messages
```

**Verification:**
- [ ] No sensitive data in logcat during sign-in flow
- [ ] No ID tokens logged
- [ ] No user emails logged

---

## Phase 3: Medium Priority Security Enhancements (Within 2 Weeks)

### Task 3.1: Encrypt Room Database
**Priority:** 🟡 MEDIUM  
**Files:**
- `app/build.gradle.kts`
- `app/src/main/java/com/pyera/app/di/DatabaseModule.kt`

**Step 1: Add SQLCipher dependency**

```kotlin
// In app/build.gradle.kts dependencies
implementation("net.zetetic:android-database-sqlcipher:4.5.4")
implementation("androidx.sqlite:sqlite-ktx:2.4.0")
```

**Step 2: Create SecurePassphraseManager**

```kotlin
package com.pyera.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SecurePassphraseManager(private val context: Context) {
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val PREFS_NAME = "secure_passphrase_prefs"
        private const val PASSPHRASE_KEY = "database_passphrase"
        private const val KEY_ALIAS = "pyera_database_key"
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
```

**Step 3: Update DatabaseModule.kt**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        securityChecker: SecurityChecker
    ): PyeraDatabase {
        val passphraseManager = SecurePassphraseManager(context)
        val passphrase = passphraseManager.getOrCreatePassphrase()
        
        val factory = SupportFactory(passphrase)
        
        return Room.databaseBuilder(
            context,
            PyeraDatabase::class.java,
            "pyera_database.db"
        )
        .openHelperFactory(factory)
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()
    }
    
    // ... rest of module
}
```

**Verification:**
- [ ] Database encrypted (check with root explorer)
- [ ] App data still accessible after encryption
- [ ] Migration handled properly

---

### Task 3.2: Add Input Validation
**Priority:** 🟡 MEDIUM  
**Files:** All ViewModels with user input

**Create Validation Utilities:**

```kotlin
package com.pyera.app.util

object ValidationUtils {
    
    fun validateTransactionNote(note: String): ValidationResult {
        return when {
            note.length > 500 -> ValidationResult.Error("Note too long (max 500 characters)")
            else -> ValidationResult.Success
        }
    }
    
    fun validateAmount(amount: Double): ValidationResult {
        return when {
            amount < 0 -> ValidationResult.Error("Amount cannot be negative")
            amount > 999_999_999 -> ValidationResult.Error("Amount exceeds maximum limit")
            else -> ValidationResult.Success
        }
    }
    
    fun validateCategoryName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Name cannot be empty")
            name.length > 50 -> ValidationResult.Error("Name too long (max 50 characters)")
            !name.matches(Regex("^[a-zA-Z0-9\\s\\-_]+$")) -> 
                ValidationResult.Error("Name contains invalid characters")
            else -> ValidationResult.Success
        }
    }
    
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
```

**Usage in ViewModels:**

```kotlin
// Example in TransactionViewModel.kt
fun addTransaction(transaction: TransactionEntity) {
    // Validate input
    val noteValidation = ValidationUtils.validateTransactionNote(transaction.note)
    if (noteValidation is ValidationUtils.ValidationResult.Error) {
        _state.update { it.copy(error = noteValidation.message) }
        return
    }
    
    val amountValidation = ValidationUtils.validateAmount(transaction.amount)
    if (amountValidation is ValidationUtils.ValidationResult.Error) {
        _state.update { it.copy(error = amountValidation.message) }
        return
    }
    
    // Proceed with adding transaction
    viewModelScope.launch {
        transactionRepository.insertTransaction(transaction)
    }
}
```

**Verification:**
- [ ] Input validation works
- [ ] Error messages shown to users
- [ ] Invalid input rejected before database operation

---

## Phase 4: Low Priority Enhancements (Nice to Have)

### Task 4.1: Add ProGuard Rules
**Priority:** 🟢 LOW  
**File:** `app/proguard-rules.pro`

```proguard
# Kimi API models
-keep class com.pyera.app.data.repository.KimiRequest { *; }
-keep class com.pyera.app.data.repository.KimiResponse { *; }
-keep class com.pyera.app.data.repository.KimiMessage { *; }
-keep class com.pyera.app.data.repository.KimiChoice { *; }
-keep class com.pyera.app.data.repository.KimiError { *; }

# Room entities
-keep class com.pyera.app.data.local.entity.* { *; }

# Serializable/Parcelable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
```

---

### Task 4.2: Add Rate Limiting for API Calls
**Priority:** 🟢 LOW  
**File:** `app/src/main/java/com/pyera/app/data/repository/ChatRepositoryImpl.kt`

```kotlin
import com.google.common.util.concurrent.RateLimiter
import java.util.concurrent.TimeUnit

@Singleton
class ChatRepositoryImpl @Inject constructor() : ChatRepository {
    
    // Rate limiter: 10 requests per minute
    private val rateLimiter = RateLimiter.create(10.0 / 60.0)
    
    override suspend fun sendMessage(prompt: String): Flow<String> = flow {
        if (!rateLimiter.tryAcquire()) {
            emit("Rate limit exceeded. Please wait a moment before sending another message.")
            return@flow
        }
        // ... rest of implementation
    }
}
```

**Add dependency:**
```kotlin
implementation("com.google.guava:guava:31.1-android")
```

---

## Security Checklist

### Pre-Release Security Checklist

- [ ] **API Keys**
  - [ ] Kimi API key rotated and secured
  - [ ] Firebase API keys restricted
  - [ ] No hardcoded keys in source code

- [ ] **Network Security**
  - [ ] Certificate pinning implemented
  - [ ] Cleartext traffic disabled
  - [ ] TLS 1.2+ enforced

- [ ] **Data Protection**
  - [ ] Room database encrypted (SQLCipher)
  - [ ] EncryptedSharedPreferences used for sensitive data
  - [ ] FLAG_SECURE prevents screenshots

- [ ] **Authentication**
  - [ ] Biometric authentication implemented
  - [ ] Secure token storage
  - [ ] Proper logout clears all data

- [ ] **Device Security**
  - [ ] Root detection implemented
  - [ ] Emulator detection implemented
  - [ ] Warning shown for insecure devices

- [ ] **Logging**
  - [ ] No sensitive data in logs
  - [ ] ProGuard strips debug logs in release
  - [ ] No ID tokens, emails, or passwords logged

- [ ] **Build Security**
  - [ ] minifyEnabled = true
  - [ ] shrinkResources = true
  - [ ] ProGuard rules configured
  - [ ] allowBackup = false

- [ ] **Input Validation**
  - [ ] Transaction amounts validated
  - [ ] Note length limits enforced
  - [ ] Category names sanitized
  - [ ] SQL injection prevention verified

---

## Testing Security Features

### Security Testing Checklist

1. **Certificate Pinning Test**
   ```bash
   # Use Charles Proxy or mitmproxy
   # Verify app fails to connect when MITM is active
   ```

2. **Root Detection Test**
   - Install on rooted device
   - Verify warning is shown

3. **Screenshot Protection Test**
   - Try taking screenshot
   - Verify black screen or error

4. **Data Encryption Test**
   ```bash
   # On rooted device or emulator
   adb shell
   cd /data/data/com.pyera.app/databases
   # Try to read database without passphrase
   ```

5. **API Key Security Test**
   - Decompile APK
   - Verify no API keys in strings or BuildConfig (when properly configured)

6. **Log Security Test**
   ```bash
   adb logcat | grep -i "token\|password\|email\|key"
   # Verify no sensitive data
   ```

---

## Resources

- [OWASP Mobile Security Testing Guide](https://owasp.org/www-project-mobile-security-testing-guide/)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [Firebase Security Rules Reference](https://firebase.google.com/docs/rules)
- [Certificate Pinning Guide](https://developer.android.com/training/articles/security-config#CertificatePinning)

---

**Next Steps:**
1. Assign tasks to team members
2. Start with Phase 1 (Critical fixes)
3. Schedule security review after implementation
4. Plan penetration testing for pre-release

---

*Generated by Security Review Agent*  
*Date: February 5, 2026*
