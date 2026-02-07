package com.pyera.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.biometric.BiometricAuthManager
import com.pyera.app.data.biometric.BiometricAuthResult
import com.pyera.app.data.biometric.BiometricCapability
import com.pyera.app.data.repository.AuthRepository
import com.pyera.app.ui.components.PasswordStrength
import com.pyera.app.ui.components.calculatePasswordStrength
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Check biometric state on initialization
        checkBiometricState()
    }

    /**
     * Check if biometric authentication is available and enabled
     */
    private fun checkBiometricState() {
        val isAvailable = biometricAuthManager.isBiometricAvailable()
        val isEnabled = repository.isBiometricEnabled()
        val hasCredentials = repository.hasStoredCredentials()
        
        _uiState.update { 
            it.copy(
                isBiometricAvailable = isAvailable,
                isBiometricEnabled = isEnabled,
                hasStoredCredentials = hasCredentials
            )
        }
    }

    /**
     * Check if biometric login can be used (available, enabled, and has stored credentials)
     */
    fun canUseBiometricLogin(): Boolean {
        return _uiState.value.isBiometricAvailable && 
               _uiState.value.isBiometricEnabled && 
               _uiState.value.hasStoredCredentials
    }

    /**
     * Perform biometric login using stored credentials
     */
    fun biometricLogin() {
        if (!canUseBiometricLogin()) {
            _uiState.update { 
                it.copy(
                    biometricAuthState = BiometricAuthState.Error("Biometric login not available")
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    authState = AuthState.Loading,
                    biometricAuthState = BiometricAuthState.Authenticating
                )
            }
            if (repository.currentUser != null) {
                _uiState.update {
                    it.copy(
                        authState = AuthState.Success,
                        biometricAuthState = BiometricAuthState.Success
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        authState = AuthState.Idle,
                        biometricAuthState = BiometricAuthState.Error("No active session. Please sign in.")
                    )
                }
            }
        }
    }

    /**
     * Handle biometric authentication result from the UI layer
     */
    fun onBiometricAuthResult(result: BiometricAuthResult) {
        when (result) {
            is BiometricAuthResult.Success -> {
                biometricLogin()
            }
            is BiometricAuthResult.Cancelled -> {
                _uiState.update { 
                    it.copy(
                        biometricAuthState = BiometricAuthState.Cancelled
                    )
                }
            }
            is BiometricAuthResult.Error -> {
                _uiState.update { 
                    it.copy(
                        biometricAuthState = BiometricAuthState.Error(result.errorMessage)
                    )
                }
            }
            is BiometricAuthResult.Failed -> {
                _uiState.update { 
                    it.copy(
                        biometricAuthState = BiometricAuthState.Error(result.message)
                    )
                }
            }
        }
    }

    /**
     * Enable biometric authentication and store credentials
     */
    fun enableBiometric(email: String) {
        val result = repository.storeCredentials(email)
        result.fold(
            onSuccess = {
                repository.setBiometricEnabled(true)
                _uiState.update { 
                    it.copy(
                        isBiometricEnabled = true,
                        hasStoredCredentials = true,
                        showBiometricPrompt = false
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { 
                    it.copy(
                        authState = AuthState.Error("Failed to store credentials: ${error.message}")
                    )
                }
            }
        )
    }

    /**
     * Disable biometric authentication and clear stored credentials
     */
    fun disableBiometric() {
        repository.setBiometricEnabled(false)
        repository.clearStoredCredentials()
        _uiState.update { 
            it.copy(
                isBiometricEnabled = false,
                hasStoredCredentials = false
            )
        }
    }

    /**
     * Show the biometric enable prompt after successful login
     */
    fun showBiometricEnablePrompt(email: String) {
        // Only show if biometric is available and not already enabled
        if (_uiState.value.isBiometricAvailable && !_uiState.value.isBiometricEnabled) {
            _uiState.update { 
                it.copy(
                    showBiometricPrompt = true,
                    pendingEmail = email
                )
            }
        }
    }

    /**
     * Dismiss the biometric enable prompt
     */
    fun dismissBiometricPrompt() {
        _uiState.update { 
            it.copy(
                showBiometricPrompt = false,
                pendingEmail = null
            )
        }
    }

    /**
     * Clear biometric auth state (after handling)
     */
    fun clearBiometricState() {
        _uiState.update { 
            it.copy(biometricAuthState = BiometricAuthState.Idle)
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authState = AuthState.Loading) }
            val result = repository.login(email, pass)
            
            _uiState.update { state ->
                if (result.isSuccess) {
                    // Check if we should show biometric prompt
                    if (state.isBiometricAvailable && !state.isBiometricEnabled) {
                        state.copy(
                            authState = AuthState.Success,
                            showBiometricPrompt = true,
                            pendingEmail = email
                        )
                    } else {
                        state.copy(authState = AuthState.Success)
                    }
                } else {
                    state.copy(
                        authState = AuthState.Error(
                            result.exceptionOrNull()?.message ?: "Login failed"
                        )
                    )
                }
            }
        }
    }

    fun register(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authState = AuthState.Loading) }
            val result = repository.register(email, pass, name)
            
            _uiState.update { 
                if (result.isSuccess) {
                    it.copy(authState = AuthState.Success)
                } else {
                    it.copy(
                        authState = AuthState.Error(
                            result.exceptionOrNull()?.message ?: "Registration failed"
                        )
                    )
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authState = AuthState.Loading) }
            val result = repository.signInWithGoogle(idToken)
            
            _uiState.update { 
                if (result.isSuccess) {
                    it.copy(authState = AuthState.Success)
                } else {
                    it.copy(
                        authState = AuthState.Error(
                            result.exceptionOrNull()?.message ?: "Google sign-in failed"
                        )
                    )
                }
            }
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun toggleRememberMe() {
        _uiState.update { it.copy(rememberMe = !it.rememberMe) }
    }

    fun toggleTermsAccepted() {
        _uiState.update { it.copy(termsAccepted = !it.termsAccepted) }
    }

    fun clearError() {
        _uiState.update { it.copy(authState = AuthState.Idle) }
    }

    fun resetState() {
        _uiState.value = AuthUiState(
            isBiometricAvailable = biometricAuthManager.isBiometricAvailable(),
            isBiometricEnabled = repository.isBiometricEnabled(),
            hasStoredCredentials = repository.hasStoredCredentials()
        )
    }

    // ==================== Validation Methods ====================
    
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email is required")
            !email.isValidEmail() -> ValidationResult.Error("Invalid email format")
            else -> ValidationResult.Success
        }
    }
    
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password is required")
            password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters")
            else -> ValidationResult.Success
        }
    }
    
    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult.Error("Please confirm your password")
            password != confirmPassword -> ValidationResult.Error("Passwords do not match")
            else -> ValidationResult.Success
        }
    }
    
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Name is required")
            name.length < 2 -> ValidationResult.Error("Name must be at least 2 characters")
            name.length > 50 -> ValidationResult.Error("Name is too long")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validate all login fields and return individual error messages
     */
    fun validateLoginFields(email: String, password: String): LoginValidationErrors {
        val emailError = when {
            email.isBlank() -> "Email is required"
            !email.isValidEmail() -> "Please enter a valid email"
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        return LoginValidationErrors(emailError, passwordError)
    }

    /**
     * Validate all registration fields and return individual error messages
     */
    fun validateRegisterFields(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean
    ): RegisterValidationErrors {
        val nameError = when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
        val emailError = when {
            email.isBlank() -> "Email is required"
            !email.isValidEmail() -> "Please enter a valid email"
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        val confirmPasswordError = when {
            confirmPassword.isBlank() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
        val termsError = if (!termsAccepted) "You must accept the terms" else null
        
        return RegisterValidationErrors(
            nameError = nameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            termsError = termsError
        )
    }
}

data class AuthUiState(
    val authState: AuthState = AuthState.Idle,
    val biometricAuthState: BiometricAuthState = BiometricAuthState.Idle,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val rememberMe: Boolean = false,
    val termsAccepted: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val hasStoredCredentials: Boolean = false,
    val showBiometricPrompt: Boolean = false,
    val pendingEmail: String? = null
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class BiometricAuthState {
    object Idle : BiometricAuthState()
    object Authenticating : BiometricAuthState()
    object Success : BiometricAuthState()
    data class Error(val message: String) : BiometricAuthState()
    object Cancelled : BiometricAuthState()
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

data class LoginValidationErrors(
    val emailError: String? = null,
    val passwordError: String? = null
)

data class RegisterValidationErrors(
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null
)

private fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
