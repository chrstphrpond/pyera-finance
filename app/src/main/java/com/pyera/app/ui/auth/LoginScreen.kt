package com.pyera.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.R
import com.pyera.app.data.biometric.BiometricAuthManager
import com.pyera.app.data.biometric.BiometricAuthResult
import com.pyera.app.data.repository.GoogleAuthHelper
import com.pyera.app.ui.components.PyeraAuthButton
import com.pyera.app.ui.components.PyeraAuthTextField
import com.pyera.app.ui.components.PyeraSocialButton
import androidx.compose.ui.res.stringResource
import com.pyera.app.R
import com.pyera.app.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
    googleAuthHelper: GoogleAuthHelper,
    biometricAuthManager: BiometricAuthManager
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState = uiState.authState
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var googleError by rememberSaveable { mutableStateOf<String?>(null) }
    
    // Field-level validation errors
    var fieldErrors by rememberSaveable { mutableStateOf(LoginValidationErrors()) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Form validation
    val isFormValid by remember(email, password) {
        androidx.compose.runtime.derivedStateOf {
            email.isValidEmail() && password.length >= 6
        }
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val signInResult = googleAuthHelper.handleSignInResult(result.data)
            signInResult.fold(
                onSuccess = { idToken ->
                    android.util.Log.d("GoogleSignIn", "Sign-in successful")
                    viewModel.signInWithGoogle(idToken)
                },
                onFailure = { e ->
                    googleError = e.message ?: "Google sign-in failed (Unknown error)"
                    android.util.Log.e("GoogleSignIn", "Sign-in failed", e)
                }
            )
        } else {
            googleError = "Google sign-in cancelled or failed"
            android.util.Log.w("GoogleSignIn", "Sign-in cancelled or failed")
        }
    }

    // Handle navigation on successful auth
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    // Show biometric prompt on launch if enabled
    LaunchedEffect(Unit) {
        if (viewModel.canUseBiometricLogin() && activity != null) {
            biometricAuthManager.showBiometricPrompt(
                activity = activity,
                title = stringResource(R.string.auth_login_biometric_title),
                subtitle = stringResource(R.string.auth_login_biometric_subtitle),
                description = stringResource(R.string.auth_login_biometric_description),
                negativeButtonText = stringResource(R.string.auth_login_biometric_negative_button),
                onResult = { result ->
                    viewModel.onBiometricAuthResult(result)
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        // Background gradient for visual interest
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DarkGreen,
                            SurfaceDark.copy(alpha = 0.8f),
                            DeepBackground
                        )
                    )
                )
        )

        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_auth_green_flow),
            contentDescription = stringResource(R.string.auth_background_content_desc),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        // Gradient Scrim for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepBackground.copy(alpha = 0.3f),
                            DeepBackground.copy(alpha = 0.7f),
                            DeepBackground
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo Section with enhanced branding
            LoginHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // Biometric Login Button (if enabled and available)
            if (uiState.isBiometricAvailable && uiState.hasStoredCredentials) {
                BiometricLoginButton(
                    onClick = {
                        activity?.let {
                            biometricAuthManager.showBiometricPrompt(
                                activity = it,
                                title = "Biometric Login",
                                subtitle = "Use your fingerprint or face to log in",
                                description = "Authenticate to access your Pyera account",
                                negativeButtonText = "Use Password",
                                onResult = { result ->
                                    viewModel.onBiometricAuthResult(result)
                                }
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Login Form Card with elevated surface
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, ColorBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                // Welcome Text
                Text(
                    text = stringResource(R.string.auth_login_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(R.string.auth_login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Field with validation
                PyeraAuthTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        googleError = null
                        fieldErrors = fieldErrors.copy(emailError = null)
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = stringResource(R.string.auth_login_email_label),
                    placeholder = stringResource(R.string.auth_login_email_placeholder),
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    isError = fieldErrors.emailError != null || authState is AuthState.Error,
                    errorMessage = fieldErrors.emailError,
                    onImeAction = { /* Focus moves to next field automatically */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field with visibility toggle
                PyeraAuthTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        googleError = null
                        fieldErrors = fieldErrors.copy(passwordError = null)
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = stringResource(R.string.auth_login_password_label),
                    placeholder = stringResource(R.string.auth_login_password_placeholder),
                    isPassword = true,
                    isPasswordVisible = uiState.isPasswordVisible,
                    onPasswordVisibilityChange = { viewModel.togglePasswordVisibility() },
                    leadingIcon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isError = fieldErrors.passwordError != null || authState is AuthState.Error,
                    errorMessage = fieldErrors.passwordError,
                    onImeAction = {
                        if (isFormValid) {
                            focusManager.clearFocus()
                            viewModel.login(email, password)
                        }
                    }
                )

                // Forgot Password Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onNavigateToForgotPassword,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.auth_login_forgot_password),
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // General Error Message (from auth state)
                AnimatedVisibility(
                    visible = authState is AuthState.Error,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (authState is AuthState.Error) {
                        Text(
                            text = authState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorError,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Google Error Message
                AnimatedVisibility(
                    visible = googleError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    googleError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorError,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login Button with loading state
                PyeraAuthButton(
                    text = stringResource(R.string.auth_login_button),
                    onClick = {
                        // Validate fields before submitting
                        val errors = viewModel.validateLoginFields(email, password)
                        fieldErrors = errors
                        
                        if (errors.emailError == null && errors.passwordError == null) {
                            focusManager.clearFocus()
                            viewModel.login(email, password)
                        }
                    },
                    isLoading = authState is AuthState.Loading,
                    enabled = authState !is AuthState.Loading
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Login Section
            SocialLoginSection(
                onGoogleSignIn = {
                    val signInIntent = googleAuthHelper.getSignInIntent()
                    if (signInIntent != null) {
                        googleSignInLauncher.launch(signInIntent)
                    } else {
                        googleError = stringResource(R.string.auth_login_google_sign_in_not_initialized)
                        android.util.Log.e("GoogleSignIn", "Sign-in intent is null")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.auth_login_no_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = stringResource(R.string.auth_login_sign_up),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonYellow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Loading Overlay
        AnimatedVisibility(
            visible = authState is AuthState.Loading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                // Loading is handled inside button, but overlay prevents interaction
            }
        }

        // Biometric Enable Dialog
        if (uiState.showBiometricPrompt) {
            BiometricEnableDialog(
                onEnable = {
                    uiState.pendingEmail?.let { pendingEmail ->
                        viewModel.enableBiometric(pendingEmail)
                    }
                },
                onDismiss = { viewModel.dismissBiometricPrompt() }
            )
        }
    }
}

@Composable
private fun LoginHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo using SVG vector drawable
        Image(
            painter = painterResource(id = R.drawable.ic_logo_full),
            contentDescription = stringResource(R.string.auth_login_logo_content_desc),
            modifier = Modifier
                .height(60.dp)
                .wrapContentWidth(),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tagline with NeonYellow accent
        Text(
            text = stringResource(R.string.auth_login_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BiometricLoginButton(
    onClick: () -> Unit
) {
    PyeraSocialButton(
        onClick = onClick,
        text = stringResource(R.string.auth_login_biometric),
        icon = {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = stringResource(R.string.auth_biometric_icon_content_desc),
                modifier = Modifier.size(24.dp),
                tint = AccentGreen
            )
        }
    )
}

@Composable
private fun BiometricEnableDialog(
    onEnable: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceElevated,
        titleContentColor = Color.White,
        textContentColor = TextSecondary,
        icon = {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = stringResource(R.string.auth_biometric_auth_content_desc),
                tint = AccentGreen,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.auth_login_biometric_enable_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.auth_login_biometric_enable_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            PyeraAuthButton(
                text = stringResource(R.string.auth_login_biometric_enable_button),
                onClick = onEnable,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.auth_login_biometric_not_now),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

@Composable
private fun SocialLoginSection(
    onGoogleSignIn: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Divider with "Or continue with" text
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = CardBorder
            )
            Text(
                text = stringResource(R.string.auth_login_or_continue_with),
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = CardBorder
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign In Button
        GoogleSignInButton(onClick = onGoogleSignIn)
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit
) {
    PyeraSocialButton(
        onClick = onClick,
        text = stringResource(R.string.auth_login_google),
        icon = {
            // Google "G" logo
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBackground
                )
            }
        }
    )
}

// Extension function for email validation
private fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
