package com.pyera.app.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.security.AppLockManager
import com.pyera.app.data.security.BiometricHelper
import com.pyera.app.data.security.SecurityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for security-related screens (App Lock, PIN setup, etc.)
 */
@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val securityPreferences: SecurityPreferences,
    private val appLockManager: AppLockManager,
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SecurityEvent>()
    val events: SharedFlow<SecurityEvent> = _events.asSharedFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        _uiState.value = SecurityUiState(
            isAppLockEnabled = securityPreferences.isAppLockEnabled,
            isBiometricEnabled = securityPreferences.useBiometric,
            isPinSet = securityPreferences.isPinSet(),
            lockTimeout = securityPreferences.lockTimeout,
            canUseBiometric = biometricHelper.canUseBiometric(),
            timeoutOptions = securityPreferences.getTimeoutOptions()
        )
    }

    /**
     * Verify PIN for unlocking the app
     */
    fun verifyPin(pin: String): Boolean {
        return if (appLockManager.verifyAndUnlock(pin)) {
            viewModelScope.launch {
                _events.emit(SecurityEvent.UnlockSuccess)
            }
            true
        } else {
            _uiState.value = _uiState.value.copy(
                pinError = "Incorrect PIN. Please try again.",
                shakePin = true
            )
            viewModelScope.launch {
                delay(300)
                _uiState.value = _uiState.value.copy(shakePin = false)
            }
            false
        }
    }

    /**
     * Set a new PIN (for first-time setup)
     */
    fun setPin(pin: String, enableBiometric: Boolean = false): Boolean {
        if (!securityPreferences.isValidPinFormat(pin)) {
            _uiState.value = _uiState.value.copy(
                pinError = "PIN must be 4-6 digits"
            )
            return false
        }

        appLockManager.setPin(pin)
        appLockManager.enableAppLock()
        
        if (enableBiometric && biometricHelper.canUseBiometric()) {
            appLockManager.setUseBiometric(true)
        }

        refreshState()
        viewModelScope.launch {
            _events.emit(SecurityEvent.PinSetSuccess)
        }
        return true
    }

    /**
     * Change PIN (requires current PIN verification)
     */
    fun changePin(currentPin: String, newPin: String): Boolean {
        if (!securityPreferences.verifyPin(currentPin)) {
            _uiState.value = _uiState.value.copy(
                pinError = "Current PIN is incorrect"
            )
            return false
        }

        if (!securityPreferences.isValidPinFormat(newPin)) {
            _uiState.value = _uiState.value.copy(
                pinError = "New PIN must be 4-6 digits"
            )
            return false
        }

        if (appLockManager.changePin(currentPin, newPin)) {
            refreshState()
            viewModelScope.launch {
                _events.emit(SecurityEvent.PinChangedSuccess)
            }
            return true
        }
        return false
    }

    /**
     * Toggle app lock on/off
     */
    fun toggleAppLock(enabled: Boolean) {
        if (enabled) {
            if (!securityPreferences.isPinSet()) {
                // Need to set PIN first
                viewModelScope.launch {
                    _events.emit(SecurityEvent.NavigateToSetPin)
                }
                return
            }
            appLockManager.enableAppLock()
        } else {
            appLockManager.disableAppLock()
        }
        refreshState()
    }

    /**
     * Toggle biometric authentication
     */
    fun toggleBiometric(enabled: Boolean) {
        if (enabled && !biometricHelper.canUseBiometric()) {
            _uiState.value = _uiState.value.copy(
                biometricError = biometricHelper.getBiometricUnavailableMessage()
            )
            return
        }
        appLockManager.setUseBiometric(enabled)
        refreshState()
    }

    /**
     * Set lock timeout
     */
    fun setLockTimeout(timeoutMs: Long) {
        appLockManager.setLockTimeout(timeoutMs)
        refreshState()
    }

    /**
     * Clear any error messages
     */
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            pinError = null,
            biometricError = null
        )
    }

    /**
     * Lock the app immediately
     */
    fun lockNow() {
        appLockManager.lock()
        viewModelScope.launch {
            _events.emit(SecurityEvent.AppLocked)
        }
    }

    /**
     * Check if biometric is available and enabled
     */
    fun canUseBiometricForUnlock(): Boolean {
        return biometricHelper.isBiometricEnabledAndAvailable()
    }

    /**
     * Attempt biometric unlock
     */
    fun unlockWithBiometric(activity: androidx.fragment.app.FragmentActivity) {
        biometricHelper.authenticateWithBiometric(
            activity = activity,
            onSuccess = {
                viewModelScope.launch {
                    _events.emit(SecurityEvent.UnlockSuccess)
                }
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    biometricError = error
                )
            },
            onCancelled = {
                // User chose to use PIN instead
            }
        )
    }

    /**
     * Get formatted timeout text
     */
    fun getTimeoutDisplayText(timeoutMs: Long): String {
        return _uiState.value.timeoutOptions.find { it.millis == timeoutMs }?.displayText 
            ?: "5 minutes"
    }
}

/**
 * UI State for security screens
 */
data class SecurityUiState(
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isPinSet: Boolean = false,
    val lockTimeout: Long = 300000L,
    val canUseBiometric: Boolean = false,
    val pinError: String? = null,
    val biometricError: String? = null,
    val shakePin: Boolean = false,
    val timeoutOptions: List<SecurityPreferences.TimeoutOption> = emptyList()
)

/**
 * Events emitted by the SecurityViewModel
 */
sealed class SecurityEvent {
    object UnlockSuccess : SecurityEvent()
    object PinSetSuccess : SecurityEvent()
    object PinChangedSuccess : SecurityEvent()
    object AppLocked : SecurityEvent()
    object NavigateToSetPin : SecurityEvent()
}
