package com.pyera.app.ui.auth

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.R
import com.pyera.app.data.biometric.BiometricAuthManager
import com.pyera.app.data.biometric.BiometricAuthResult
import com.pyera.app.data.repository.GoogleAuthHelper
import com.pyera.app.ui.components.ButtonSize
import com.pyera.app.ui.components.PyeraButton
import com.pyera.app.ui.components.PyeraTextField
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.CardBorder
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.NeonYellow
import com.pyera.app.ui.theme.Radius
import com.pyera.app.ui.theme.Spacing
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextSecondary
import com.pyera.app.ui.theme.TextTertiary

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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var googleError by remember { mutableStateOf<String?>(null) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_auth_green_flow),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.6f
        )

        // Gradient Scrim for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepBackground.copy(alpha = 0.4f),
                            DeepBackground.copy(alpha = 0.8f),
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
                .padding(horizontal = Spacing.XLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.XXXLarge))

            // Logo Section
            LoginHeader()

            Spacer(modifier = Modifier.height(Spacing.XLarge))

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
                Spacer(modifier = Modifier.height(Spacing.Medium))
            }

            // Login Form Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.xl))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(Radius.xl))
                    .padding(Spacing.XLarge)
            ) {
                // Welcome Text
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.XLarge))

                // Email Field using PyeraTextField
                PyeraTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        googleError = null
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = "Email",
                    placeholder = "Enter your email",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    isError = authState is AuthState.Error
                )

                Spacer(modifier = Modifier.height(Spacing.Large))

                // Password Field using PyeraTextField with visibility toggle
                PyeraTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        googleError = null
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = "Password",
                    placeholder = "Enter your password",
                    leadingIcon = Icons.Default.Lock,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                            Icon(
                                imageVector = if (uiState.isPasswordVisible)
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (uiState.isPasswordVisible)
                                    "Hide password" else "Show password",
                                tint = TextTertiary
                            )
                        }
                    },
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    visualTransformation = if (uiState.isPasswordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    onDone = {
                        if (isFormValid) {
                            focusManager.clearFocus()
                            viewModel.login(email, password)
                        }
                    },
                    isError = authState is AuthState.Error
                )

                Spacer(modifier = Modifier.height(Spacing.Small))

                // Forgot Password Link
                TextButton(
                    onClick = onNavigateToForgotPassword,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentGreen
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Small))

                // Error Message
                if (authState is AuthState.Error) {
                    Text(
                        text = authState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorError,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Spacing.Small))
                }

                // Google Error Message
                googleError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorError,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Spacing.Small))
                }

                Spacer(modifier = Modifier.height(Spacing.Large))

                // Login Button using PyeraButton
                PyeraButton(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.login(email, password)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    size = ButtonSize.Large,
                    enabled = isFormValid && authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DeepBackground,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.XLarge))

            // Social Login Section
            SocialLoginSection(
                onGoogleSignIn = {
                    val signInIntent = googleAuthHelper.getSignInIntent()
                    if (signInIntent != null) {
                        googleSignInLauncher.launch(signInIntent)
                    } else {
                        googleError = "Google Sign-In not initialized"
                        android.util.Log.e("GoogleSignIn", "Sign-in intent is null")
                    }
                }
            )

            Spacer(modifier = Modifier.height(Spacing.XLarge))

            // Sign Up Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonYellow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.XXLarge))
        }

        // Biometric Enable Dialog
        if (uiState.showBiometricPrompt) {
            BiometricEnableDialog(
                onEnable = {
                    uiState.pendingEmail?.let { pendingEmail ->
                        uiState.pendingPassword?.let { pendingPassword ->
                            viewModel.enableBiometric(pendingEmail, pendingPassword)
                        }
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
            contentDescription = "Pyera Logo",
            modifier = Modifier
                .height(60.dp)
                .wrapContentWidth(),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(Spacing.Large))

        // Tagline
        Text(
            text = "Your Personal Finance Assistant",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun BiometricLoginButton(
    onClick: () -> Unit
) {
    PyeraButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        size = ButtonSize.Large
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.Small))
            Text(
                text = "Login with Biometrics",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BiometricEnableDialog(
    onEnable: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        titleContentColor = Color.White,
        textContentColor = TextSecondary,
        icon = {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp)
            )
        },
        title = {
            Text(
                text = "Enable Biometric Login?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "Use your fingerprint or face recognition for faster and more secure login next time.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            PyeraButton(
                onClick = onEnable,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enable",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Not Now",
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
                text = "  Or continue with  ",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = CardBorder
            )
        }

        Spacer(modifier = Modifier.height(Spacing.Large))

        // Google Sign In Button
        GoogleSignInButton(onClick = onGoogleSignIn)
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit
) {
    PyeraButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        size = ButtonSize.Large
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
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
            Spacer(modifier = Modifier.width(Spacing.Small))
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Extension function for email validation
private fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
