package com.pyera.app.data.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.pyera.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the app lock state and lifecycle.
 * Tracks app background/foreground to determine when to show the lock screen.
 * Also handles session timeout for Firebase Auth.
 */
@Singleton
class AppLockManager @Inject constructor(
    private val securityPreferences: SecurityPreferences,
    private val authRepository: AuthRepository
) : DefaultLifecycleObserver {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isAppLockEnabled = MutableStateFlow(false)
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private var isAppInBackground = false

    init {
        // Initialize state from preferences
        _isAppLockEnabled.value = securityPreferences.isAppLockEnabled
    }

    companion object {
        const val SESSION_TIMEOUT_MS = 30 * 60 * 1000L // 30 minutes
    }

    /**
     * Called when app goes to background
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppInBackground = true
        securityPreferences.lastActiveTime = System.currentTimeMillis()
    }

    /**
     * Called when app comes to foreground
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (isAppInBackground) {
            // Check session timeout first
            if (isSessionExpired()) {
                // Sign out user due to session timeout
                authRepository.logout()
            } else if (shouldLock()) {
                lock()
            }
        }
        isAppInBackground = false
    }

    /**
     * Check if the Firebase Auth session has expired (30 minutes of inactivity)
     */
    fun isSessionExpired(): Boolean {
        // Only check if user is logged in
        if (authRepository.currentUser == null) return false
        
        val elapsed = System.currentTimeMillis() - securityPreferences.lastActiveTime
        return elapsed > SESSION_TIMEOUT_MS
    }

    /**
     * Check if the app should be locked based on timeout
     */
    fun shouldLock(): Boolean {
        if (!securityPreferences.isAppLockEnabled) return false
        if (!securityPreferences.isPinSet()) return false

        val elapsed = System.currentTimeMillis() - securityPreferences.lastActiveTime
        return elapsed > securityPreferences.lockTimeout
    }

    /**
     * Immediately lock the app
     */
    fun lock() {
        if (securityPreferences.isAppLockEnabled && securityPreferences.isPinSet()) {
            _isLocked.value = true
        }
    }

    /**
     * Unlock the app
     */
    fun unlock() {
        _isLocked.value = false
        securityPreferences.lastActiveTime = System.currentTimeMillis()
    }

    /**
     * Check if the app is currently locked
     */
    fun isCurrentlyLocked(): Boolean = _isLocked.value

    /**
     * Verify PIN and unlock if correct
     * Resets failed attempts on success, records failed attempt on failure
     * @return true if PIN is correct and app is unlocked
     */
    fun verifyAndUnlock(pin: String): Boolean {
        // Check if locked out first
        if (securityPreferences.isLockedOut()) {
            return false
        }
        
        return if (securityPreferences.verifyPin(pin)) {
            securityPreferences.resetAttempts()
            unlock()
            true
        } else {
            securityPreferences.recordFailedAttempt()
            false
        }
    }

    /**
     * Check if the user is currently locked out from PIN entry
     */
    fun isLockedOut(): Boolean = securityPreferences.isLockedOut()

    /**
     * Get the remaining lockout time in milliseconds
     */
    fun getRemainingLockoutTime(): Long = securityPreferences.getRemainingLockoutTime()

    /**
     * Get the number of remaining PIN attempts before lockout
     */
    fun getRemainingAttempts(): Int = securityPreferences.getRemainingAttempts()

    /**
     * Enable app lock
     */
    fun enableAppLock() {
        securityPreferences.isAppLockEnabled = true
        _isAppLockEnabled.value = true
    }

    /**
     * Disable app lock
     */
    fun disableAppLock() {
        securityPreferences.isAppLockEnabled = false
        securityPreferences.pinCode = null
        securityPreferences.useBiometric = false
        _isAppLockEnabled.value = false
        _isLocked.value = false
    }

    /**
     * Set the PIN code
     */
    fun setPin(pin: String): Boolean {
        if (!securityPreferences.isValidPinFormat(pin)) {
            return false
        }
        securityPreferences.pinCode = pin
        return true
    }

    /**
     * Change the PIN code (requires current PIN verification)
     */
    fun changePin(currentPin: String, newPin: String): Boolean {
        if (!securityPreferences.verifyPin(currentPin)) {
            return false
        }
        if (!securityPreferences.isValidPinFormat(newPin)) {
            return false
        }
        securityPreferences.pinCode = newPin
        return true
    }

    /**
     * Toggle biometric authentication
     */
    fun setUseBiometric(enabled: Boolean) {
        securityPreferences.useBiometric = enabled
    }

    /**
     * Check if biometric is enabled
     */
    fun isBiometricEnabled(): Boolean = securityPreferences.useBiometric

    /**
     * Set the lock timeout
     */
    fun setLockTimeout(timeoutMs: Long) {
        securityPreferences.lockTimeout = timeoutMs
    }

    /**
     * Get the current lock timeout
     */
    fun getLockTimeout(): Long = securityPreferences.lockTimeout

    /**
     * Refresh the enabled state from preferences
     */
    fun refreshState() {
        _isAppLockEnabled.value = securityPreferences.isAppLockEnabled
    }

    /**
     * Check if app lock is fully configured (enabled + PIN set)
     */
    fun isAppLockFullyConfigured(): Boolean {
        return securityPreferences.isAppLockEnabled && securityPreferences.isPinSet()
    }

    /**
     * Check if this is the first time setup (no PIN set yet)
     */
    fun isFirstTimeSetup(): Boolean {
        return !securityPreferences.isPinSet()
    }
}
