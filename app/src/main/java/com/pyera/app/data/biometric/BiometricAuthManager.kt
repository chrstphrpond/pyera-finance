package com.pyera.app.data.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result class representing the status of biometric capability on the device
 */
sealed class BiometricCapability {
    object Available : BiometricCapability()
    object NoHardware : BiometricCapability()
    object HardwareUnavailable : BiometricCapability()
    object NotEnrolled : BiometricCapability()
    object SecurityUpdateRequired : BiometricCapability()
    data class Unknown(val error: String) : BiometricCapability()
}

/**
 * Result class representing the outcome of a biometric authentication attempt
 */
sealed class BiometricAuthResult {
    object Success : BiometricAuthResult()
    object Cancelled : BiometricAuthResult()
    data class Error(val errorCode: Int, val errorMessage: String) : BiometricAuthResult()
    data class Failed(val message: String) : BiometricAuthResult()
}

/**
 * Manager class that wraps AndroidX Biometric library functionality.
 * Handles checking device capability and showing biometric prompt.
 */
@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val biometricManager = BiometricManager.from(context)

    /**
     * Checks if the device supports biometric authentication and if biometrics are enrolled
     */
    fun checkBiometricCapability(): BiometricCapability {
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapability.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricCapability.SecurityUpdateRequired
            else -> BiometricCapability.Unknown("Unknown biometric status")
        }
    }

    /**
     * Returns true if biometric authentication is available and enrolled on this device
     */
    fun isBiometricAvailable(): Boolean {
        return checkBiometricCapability() == BiometricCapability.Available
    }

    /**
     * Shows the biometric prompt to the user
     *
     * @param activity The FragmentActivity to show the prompt in
     * @param title The title shown in the biometric prompt
     * @param subtitle The subtitle shown in the biometric prompt
     * @param description The description shown in the biometric prompt
     * @param negativeButtonText Text for the negative button (cancel button)
     * @param onResult Callback for the authentication result
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Biometric Authentication",
        subtitle: String = "Verify your identity",
        description: String = "Use your biometric credential to authenticate",
        negativeButtonText: String = "Cancel",
        onResult: (BiometricAuthResult) -> Unit
    ) {
        if (!isBiometricAvailable()) {
            onResult(BiometricAuthResult.Error(
                BiometricPrompt.ERROR_NO_BIOMETRICS,
                "Biometric authentication is not available"
            ))
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onResult(BiometricAuthResult.Success)
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            onResult(BiometricAuthResult.Cancelled)
                        }
                        else -> {
                            onResult(BiometricAuthResult.Error(errorCode, errString.toString()))
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(BiometricAuthResult.Failed("Authentication failed"))
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Gets a user-friendly error message based on the biometric capability status
     */
    fun getCapabilityErrorMessage(capability: BiometricCapability): String {
        return when (capability) {
            is BiometricCapability.NoHardware -> 
                "This device doesn't support biometric authentication"
            is BiometricCapability.HardwareUnavailable -> 
                "Biometric hardware is currently unavailable"
            is BiometricCapability.NotEnrolled -> 
                "No biometric credentials are enrolled. Please set up biometrics in Settings"
            is BiometricCapability.SecurityUpdateRequired -> 
                "A security update is required for biometric authentication"
            is BiometricCapability.Unknown -> 
                "Unable to determine biometric availability"
            else -> ""
        }
    }
}
