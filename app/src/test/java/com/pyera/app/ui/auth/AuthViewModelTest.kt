package com.pyera.app.ui.auth

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.pyera.app.data.biometric.BiometricAuthManager
import com.pyera.app.data.biometric.BiometricAuthResult
import com.pyera.app.domain.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var authRepository: AuthRepository
    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        authRepository = mockk(relaxed = true)
        biometricAuthManager = mockk(relaxed = true)
        mockFirebaseUser = mockk(relaxed = true)
        
        // Default biometric setup
        every { biometricAuthManager.isBiometricAvailable() } returns true
        every { authRepository.isBiometricEnabled() } returns false
        every { authRepository.hasStoredCredentials() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial state has default values`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Idle)
            assertTrue(state.biometricAuthState is BiometricAuthState.Idle)
            assertFalse(state.isPasswordVisible)
            assertFalse(state.isConfirmPasswordVisible)
            assertFalse(state.rememberMe)
            assertFalse(state.termsAccepted)
        }
    }

    @Test
    fun `initial state checks biometric availability`() = runTest(testDispatcher) {
        // Given
        every { biometricAuthManager.isBiometricAvailable() } returns true
        every { authRepository.isBiometricEnabled() } returns true
        every { authRepository.hasStoredCredentials() } returns true
        
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isBiometricAvailable)
            assertTrue(state.isBiometricEnabled)
            assertTrue(state.hasStoredCredentials)
        }
    }

    // ==================== Login Tests ====================

    @Test
    fun `login sets Loading state initially`() = runTest(testDispatcher) {
        // Given
        every { authRepository.login(any(), any()) } returns Result.success(mockFirebaseUser)
        viewModel = createViewModel()
        
        // When
        viewModel.login("test@example.com", "password123")
        
        // Then - immediate check
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Loading)
        }
    }

    @Test
    fun `login sets Success state on successful login`() = runTest(testDispatcher) {
        // Given
        every { authRepository.login(any(), any()) } returns Result.success(mockFirebaseUser)
        every { biometricAuthManager.isBiometricAvailable() } returns false
        viewModel = createViewModel()
        
        // When
        viewModel.login("test@example.com", "password123")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Success)
        }
    }

    @Test
    fun `login sets Error state on failure`() = runTest(testDispatcher) {
        // Given
        every { authRepository.login(any(), any()) } returns Result.failure(Exception("Invalid credentials"))
        viewModel = createViewModel()
        
        // When
        viewModel.login("test@example.com", "wrongpassword")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Error)
            assertEquals("Invalid credentials", (state.authState as AuthState.Error).message)
        }
    }

    @Test
    fun `login shows biometric prompt when available and not enabled`() = runTest(testDispatcher) {
        // Given
        every { authRepository.login(any(), any()) } returns Result.success(mockFirebaseUser)
        every { biometricAuthManager.isBiometricAvailable() } returns true
        every { authRepository.isBiometricEnabled() } returns false
        viewModel = createViewModel()
        
        // When
        viewModel.login("test@example.com", "password123")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.showBiometricPrompt)
            assertEquals("test@example.com", state.pendingEmail)
        }
    }

    // ==================== Register Tests ====================

    @Test
    fun `register sets Loading state initially`() = runTest(testDispatcher) {
        // Given
        every { authRepository.register(any(), any(), any()) } returns Result.success(mockFirebaseUser)
        viewModel = createViewModel()
        
        // When
        viewModel.register("test@example.com", "password123", "Test User")
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Loading)
        }
    }

    @Test
    fun `register sets Success state on successful registration`() = runTest(testDispatcher) {
        // Given
        every { authRepository.register(any(), any(), any()) } returns Result.success(mockFirebaseUser)
        viewModel = createViewModel()
        
        // When
        viewModel.register("test@example.com", "password123", "Test User")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Success)
        }
    }

    @Test
    fun `register sets Error state on failure`() = runTest(testDispatcher) {
        // Given
        every { authRepository.register(any(), any(), any()) } returns Result.failure(Exception("Email already exists"))
        viewModel = createViewModel()
        
        // When
        viewModel.register("existing@example.com", "password123", "Test User")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Error)
            assertEquals("Email already exists", (state.authState as AuthState.Error).message)
        }
    }

    // ==================== Google Sign In Tests ====================

    @Test
    fun `signInWithGoogle sets Success state on success`() = runTest(testDispatcher) {
        // Given
        every { authRepository.signInWithGoogle(any()) } returns Result.success(mockFirebaseUser)
        viewModel = createViewModel()
        
        // When
        viewModel.signInWithGoogle("fake_id_token")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Success)
        }
    }

    @Test
    fun `signInWithGoogle sets Error state on failure`() = runTest(testDispatcher) {
        // Given
        every { authRepository.signInWithGoogle(any()) } returns Result.failure(Exception("Google sign-in failed"))
        viewModel = createViewModel()
        
        // When
        viewModel.signInWithGoogle("fake_id_token")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Error)
            assertEquals("Google sign-in failed", (state.authState as AuthState.Error).message)
        }
    }

    // ==================== Biometric Tests ====================

    @Test
    fun `canUseBiometricLogin returns true when all conditions met`() = runTest(testDispatcher) {
        // Given
        every { biometricAuthManager.isBiometricAvailable() } returns true
        every { authRepository.isBiometricEnabled() } returns true
        every { authRepository.hasStoredCredentials() } returns true
        viewModel = createViewModel()
        
        // Then
        assertTrue(viewModel.canUseBiometricLogin())
    }

    @Test
    fun `canUseBiometricLogin returns false when biometric not available`() = runTest(testDispatcher) {
        // Given
        every { biometricAuthManager.isBiometricAvailable() } returns false
        every { authRepository.isBiometricEnabled() } returns true
        every { authRepository.hasStoredCredentials() } returns true
        viewModel = createViewModel()
        
        // Then
        assertFalse(viewModel.canUseBiometricLogin())
    }

    @Test
    fun `canUseBiometricLogin returns false when biometric not enabled`() = runTest(testDispatcher) {
        // Given
        every { biometricAuthManager.isBiometricAvailable() } returns true
        every { authRepository.isBiometricEnabled() } returns false
        every { authRepository.hasStoredCredentials() } returns true
        viewModel = createViewModel()
        
        // Then
        assertFalse(viewModel.canUseBiometricLogin())
    }

    @Test
    fun `biometricLogin sets error when cannot use biometric`() = runTest(testDispatcher) {
        // Given
        every { biometricAuthManager.isBiometricAvailable() } returns false
        viewModel = createViewModel()
        
        // When
        viewModel.biometricLogin()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.biometricAuthState is BiometricAuthState.Error)
        }
    }

    @Test
    fun `biometricLogin sets Success when current user exists`() = runTest(testDispatcher) {
        // Given
        every { biometricAuthManager.isBiometricAvailable() } returns true
        every { authRepository.isBiometricEnabled() } returns true
        every { authRepository.hasStoredCredentials() } returns true
        every { authRepository.currentUser } returns mockFirebaseUser
        viewModel = createViewModel()
        
        // When
        viewModel.biometricLogin()
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.biometricAuthState is BiometricAuthState.Success)
            assertTrue(state.authState is AuthState.Success)
        }
    }

    @Test
    fun `onBiometricAuthResult with Success triggers biometricLogin`() = runTest(testDispatcher) {
        // Given
        every { biometricAuthManager.isBiometricAvailable() } returns true
        every { authRepository.isBiometricEnabled() } returns true
        every { authRepository.hasStoredCredentials() } returns true
        every { authRepository.currentUser } returns mockFirebaseUser
        viewModel = createViewModel()
        
        // When
        viewModel.onBiometricAuthResult(BiometricAuthResult.Success)
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.biometricAuthState is BiometricAuthState.Success)
        }
    }

    @Test
    fun `onBiometricAuthResult with Cancelled sets Cancelled state`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.onBiometricAuthResult(BiometricAuthResult.Cancelled)
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.biometricAuthState is BiometricAuthState.Cancelled)
        }
    }

    @Test
    fun `onBiometricAuthResult with Error sets Error state`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.onBiometricAuthResult(BiometricAuthResult.Error(1, "Biometric error"))
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.biometricAuthState is BiometricAuthState.Error)
            assertEquals("Biometric error", (state.biometricAuthState as BiometricAuthState.Error).message)
        }
    }

    @Test
    fun `onBiometricAuthResult with Failed sets Error state`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.onBiometricAuthResult(BiometricAuthResult.Failed("Auth failed"))
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.biometricAuthState is BiometricAuthState.Error)
            assertEquals("Auth failed", (state.biometricAuthState as BiometricAuthState.Error).message)
        }
    }

    @Test
    fun `enableBiometric stores credentials and enables`() = runTest(testDispatcher) {
        // Given
        every { authRepository.storeCredentials(any()) } returns Result.success(Unit)
        viewModel = createViewModel()
        
        // When
        viewModel.enableBiometric("test@example.com")
        
        // Then
        verify { authRepository.storeCredentials("test@example.com") }
        verify { authRepository.setBiometricEnabled(true) }
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isBiometricEnabled)
            assertTrue(state.hasStoredCredentials)
            assertFalse(state.showBiometricPrompt)
        }
    }

    @Test
    fun `enableBiometric sets error on failure`() = runTest(testDispatcher) {
        // Given
        every { authRepository.storeCredentials(any()) } returns Result.failure(Exception("Storage failed"))
        viewModel = createViewModel()
        
        // When
        viewModel.enableBiometric("test@example.com")
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Error)
            assertTrue((state.authState as AuthState.Error).message.contains("Storage failed"))
        }
    }

    @Test
    fun `disableBiometric clears credentials and disables`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.disableBiometric()
        
        // Then
        verify { authRepository.setBiometricEnabled(false) }
        verify { authRepository.clearStoredCredentials() }
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isBiometricEnabled)
            assertFalse(state.hasStoredCredentials)
        }
    }

    @Test
    fun `showBiometricEnablePrompt shows when available and not enabled`() = runTest(testDispatcher) {
        // Given
        every { biometricAuthManager.isBiometricAvailable() } returns true
        every { authRepository.isBiometricEnabled() } returns false
        viewModel = createViewModel()
        
        // When
        viewModel.showBiometricEnablePrompt("test@example.com")
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.showBiometricPrompt)
            assertEquals("test@example.com", state.pendingEmail)
        }
    }

    @Test
    fun `showBiometricEnablePrompt does not show when already enabled`() = runTest(testDispatcher) {
        // Given
        every { biometricAuthManager.isBiometricAvailable() } returns true
        every { authRepository.isBiometricEnabled() } returns true
        viewModel = createViewModel()
        
        // When
        viewModel.showBiometricEnablePrompt("test@example.com")
        
        // Then - state should not change (no prompt shown)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.showBiometricPrompt)
        }
    }

    @Test
    fun `dismissBiometricPrompt hides prompt`() = runTest(testDispatcher) {
        // Given
        every { biometricAuthManager.isBiometricAvailable() } returns true
        every { authRepository.isBiometricEnabled() } returns false
        viewModel = createViewModel()
        viewModel.showBiometricEnablePrompt("test@example.com")
        
        // When
        viewModel.dismissBiometricPrompt()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.showBiometricPrompt)
            assertNull(state.pendingEmail)
        }
    }

    @Test
    fun `clearBiometricState resets to Idle`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.onBiometricAuthResult(BiometricAuthResult.Error(1, "Error"))
        
        // When
        viewModel.clearBiometricState()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.biometricAuthState is BiometricAuthState.Idle)
        }
    }

    // ==================== Toggle Tests ====================

    @Test
    fun `togglePasswordVisibility toggles state`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When - toggle on
        viewModel.togglePasswordVisibility()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isPasswordVisible)
        }
        
        // When - toggle off
        viewModel.togglePasswordVisibility()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isPasswordVisible)
        }
    }

    @Test
    fun `toggleConfirmPasswordVisibility toggles state`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.toggleConfirmPasswordVisibility()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isConfirmPasswordVisible)
        }
    }

    @Test
    fun `toggleRememberMe toggles state`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.toggleRememberMe()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.rememberMe)
        }
    }

    @Test
    fun `toggleTermsAccepted toggles state`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.toggleTermsAccepted()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.termsAccepted)
        }
    }

    // ==================== Clear Error Tests ====================

    @Test
    fun `clearError resets auth state to Idle`() = runTest(testDispatcher) {
        // Given
        every { authRepository.login(any(), any()) } returns Result.failure(Exception("Error"))
        viewModel = createViewModel()
        viewModel.login("test@example.com", "wrong")
        advanceUntilIdle()
        
        // When
        viewModel.clearError()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Idle)
        }
    }

    // ==================== Reset State Tests ====================

    @Test
    fun `resetState resets to initial state`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.togglePasswordVisibility()
        viewModel.toggleRememberMe()
        
        // When
        viewModel.resetState()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.authState is AuthState.Idle)
            assertFalse(state.isPasswordVisible)
            assertFalse(state.rememberMe)
        }
    }

    // ==================== Validation Tests ====================

    @Test
    fun `validateEmail returns Success for valid email`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateEmail("test@example.com")
        
        // Then
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `validateEmail returns Error for blank email`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateEmail("")
        
        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Email is required", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateEmail returns Error for invalid format`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateEmail("invalid-email")
        
        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Invalid email format", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validatePassword returns Success for valid password`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validatePassword("password123")
        
        // Then
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `validatePassword returns Error for blank password`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validatePassword("")
        
        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Password is required", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validatePassword returns Error for short password`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validatePassword("12345")
        
        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Password must be at least 6 characters", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateConfirmPassword returns Success when matching`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateConfirmPassword("password123", "password123")
        
        // Then
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `validateConfirmPassword returns Error when not matching`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateConfirmPassword("password123", "different")
        
        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Passwords do not match", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateName returns Success for valid name`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateName("John Doe")
        
        // Then
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `validateName returns Error for blank name`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateName("")
        
        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Name is required", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateName returns Error for short name`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateName("A")
        
        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Name must be at least 2 characters", (result as ValidationResult.Error).message)
    }

    @Test
    fun `validateName returns Error for too long name`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateName("A".repeat(51))
        
        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Name is too long", (result as ValidationResult.Error).message)
    }

    // ==================== Field Validation Tests ====================

    @Test
    fun `validateLoginFields returns no errors for valid input`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateLoginFields("test@example.com", "password123")
        
        // Then
        assertNull(result.emailError)
        assertNull(result.passwordError)
    }

    @Test
    fun `validateLoginFields returns errors for invalid input`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateLoginFields("", "123")
        
        // Then
        assertNotNull(result.emailError)
        assertNotNull(result.passwordError)
    }

    @Test
    fun `validateRegisterFields returns no errors for valid input`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateRegisterFields(
            name = "John Doe",
            email = "test@example.com",
            password = "password123",
            confirmPassword = "password123",
            termsAccepted = true
        )
        
        // Then
        assertNull(result.nameError)
        assertNull(result.emailError)
        assertNull(result.passwordError)
        assertNull(result.confirmPasswordError)
        assertNull(result.termsError)
    }

    @Test
    fun `validateRegisterFields returns all errors for invalid input`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateRegisterFields(
            name = "",
            email = "invalid",
            password = "123",
            confirmPassword = "456",
            termsAccepted = false
        )
        
        // Then
        assertNotNull(result.nameError)
        assertNotNull(result.emailError)
        assertNotNull(result.passwordError)
        assertNotNull(result.confirmPasswordError)
        assertNotNull(result.termsError)
    }

    private fun createViewModel(): AuthViewModel {
        return AuthViewModel(
            repository = authRepository,
            biometricAuthManager = biometricAuthManager
        )
    }
}
