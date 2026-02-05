package com.pyera.app.data.security

import androidx.fragment.app.FragmentActivity
import com.pyera.app.data.biometric.BiometricAuthManager
import com.pyera.app.data.biometric.BiometricAuthResult
import com.pyera.app.data.biometric.BiometricCapability
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for biometric authentication in the app lock flow.
 * Wraps the BiometricAuthManager with app lock specific functionality.
 */
@Singleton
class BiometricHelper @Inject constructor(
    private val biometricAuthManager: BiometricAuthManager,
    private val appLockManager: AppLockManager
) {

    /**
     * Check if biometric authentication can be used
     */
    fun canUseBiometric(): Boolean {
        return biometricAuthManager.isBiometricAvailable()
    }

    /**
     * Get the biometric capability status
     */
    fun checkBiometricCapability(): BiometricCapability {
        return biometricAuthManager.checkBiometricCapability()
    }

    /**
     * Authenticate with biometric and unlock the app if successful
     */
    fun authenticateWithBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
        onCancelled: () -> Unit = {}
    ) {
        if (!canUseBiometric()) {
            onError("Biometric authentication is not available on this device")
            return
        }

        biometricAuthManager.showBiometricPrompt(
            activity = activity,
            title = "Unlock Pyera",
            subtitle = "Use your biometric to access the app",
            description = "Verify your identity to continue",
            negativeButtonText = "Use PIN",
            onResult = { result ->
                when (result) {
                    is BiometricAuthResult.Success -> {
                        appLockManager.unlock()
                        onSuccess()
                    }
                    is BiometricAuthResult.Cancelled,
                    is BiometricAuthResult.Error -> {
                        if (result is BiometricAuthResult.Error && 
                            (result.errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                             result.errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON)) {
                            onCancelled()
                        } else {
                            onError("Authentication cancelled")
                        }
                    }
                    is BiometricAuthResult.Failed -> {
                        onError(result.message)
                    }
                }
            }
        )
    }

    /**
     * Check if biometric is enabled in settings and available on device
     */
    fun isBiometricEnabledAndAvailable(): Boolean {
        return appLockManager.isBiometricEnabled() && canUseBiometric()
    }

    /**
     * Get a user-friendly message about why biometric is unavailable
     */
    fun getBiometricUnavailableMessage(): String {
        val capability = checkBiometricCapability()
        return biometricAuthManager.getCapabilityErrorMessage(capability)
    }

    companion object {
        // Import error codes from BiometricPrompt for convenience
        private val BiometricPrompt = androidx.biometric.BiometricPrompt
    }
}
