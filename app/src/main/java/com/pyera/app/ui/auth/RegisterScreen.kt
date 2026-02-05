package com.pyera.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.R
import com.pyera.app.ui.components.ButtonSize
import com.pyera.app.ui.components.PyeraButton
import com.pyera.app.ui.components.PyeraTextField
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.CardBorder
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.ColorSuccess
import com.pyera.app.ui.theme.ColorWarning
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.NeonYellow
import com.pyera.app.ui.theme.Radius
import com.pyera.app.ui.theme.Spacing
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextSecondary
import com.pyera.app.ui.theme.TextTertiary

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState = uiState.authState

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Validation states
    val passwordsMatch by remember(confirmPassword, password) {
        derivedStateOf {
            confirmPassword.isEmpty() || password == confirmPassword
        }
    }
    
    val passwordStrength by remember(password) {
        derivedStateOf { calculatePasswordStrength(password) }
    }
    
    val isFormValid by remember(name, email, password, confirmPassword, passwordsMatch, uiState.termsAccepted) {
        derivedStateOf {
            name.isNotBlank() &&
            email.isValidEmail() &&
            password.length >= 6 &&
            confirmPassword.isNotBlank() &&
            passwordsMatch &&
            uiState.termsAccepted
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
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
            Spacer(modifier = Modifier.height(Spacing.XXLarge))

            // Top Section - Logo and Branding
            RegisterHeader()

            Spacer(modifier = Modifier.height(Spacing.XLarge))

            // Registration Form Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.xl))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(Radius.xl))
                    .padding(Spacing.XLarge)
            ) {
                // Create Account Text
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Sign up to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.XLarge))

                // Full Name Field using PyeraTextField
                PyeraTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    leadingIcon = Icons.Default.Person,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(Spacing.Large))

                // Email Field using PyeraTextField
                PyeraTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = "Email",
                    placeholder = "Enter your email",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(Spacing.Large))

                // Password Field using PyeraTextField
                PyeraTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = "Password",
                    placeholder = "Create a password",
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
                    imeAction = ImeAction.Next,
                    visualTransformation = if (uiState.isPasswordVisible)
                        VisualTransformation.None else PasswordVisualTransformation()
                )

                // Password Strength Indicator
                AnimatedVisibility(
                    visible = password.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(Spacing.Small))
                        PasswordStrengthIndicator(strength = passwordStrength)
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.Large))

                // Confirm Password Field using PyeraTextField
                PyeraTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = "Confirm Password",
                    placeholder = "Confirm your password",
                    leadingIcon = Icons.Default.Lock,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                            Icon(
                                imageVector = if (uiState.isConfirmPasswordVisible)
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (uiState.isConfirmPasswordVisible)
                                    "Hide password" else "Show password",
                                tint = TextTertiary
                            )
                        }
                    },
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    visualTransformation = if (uiState.isConfirmPasswordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    onDone = {
                        if (isFormValid) {
                            focusManager.clearFocus()
                            viewModel.register(email, password, name)
                        }
                    },
                    isError = !passwordsMatch && confirmPassword.isNotEmpty(),
                    errorMessage = if (!passwordsMatch && confirmPassword.isNotEmpty()) "Passwords do not match" else null
                )

                Spacer(modifier = Modifier.height(Spacing.Large))

                // Terms & Conditions Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = uiState.termsAccepted,
                        onCheckedChange = { viewModel.toggleTermsAccepted() },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = AccentGreen,
                            uncheckedColor = TextTertiary,
                            checkmarkColor = DeepBackground
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = Spacing.Small)
                    ) {
                        Text(
                            text = "I accept the ",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        TextButton(
                            onClick = { /* TODO: Navigate to Terms */ },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Terms & Conditions",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentGreen
                            )
                        }
                    }
                }

                // Error Message
                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(Spacing.Small))
                    Text(
                        text = authState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorError,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.XLarge))

                // Create Account Button using PyeraButton
                PyeraButton(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.register(email, password, name)
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
                            text = "Sign Up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.XLarge))

            // Login Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonYellow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.XXLarge))
        }
    }
}

@Composable
private fun RegisterHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(Radius.lg))
                .background(AccentGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "₱",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBackground
            )
        }

        Spacer(modifier = Modifier.height(Spacing.Large))

        // Title
        Text(
            text = "Join Pyera",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(Spacing.XSmall))

        // Subtitle
        Text(
            text = "Start your financial journey",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val progress = when (strength) {
        PasswordStrength.EMPTY -> 0f
        PasswordStrength.WEAK -> 0.33f
        PasswordStrength.MEDIUM -> 0.66f
        PasswordStrength.STRONG -> 1f
    }

    val color = when (strength) {
        PasswordStrength.EMPTY -> CardBorder
        PasswordStrength.WEAK -> ColorError
        PasswordStrength.MEDIUM -> ColorWarning
        PasswordStrength.STRONG -> ColorSuccess
    }

    val label = when (strength) {
        PasswordStrength.EMPTY -> ""
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.MEDIUM -> "Medium"
        PasswordStrength.STRONG -> "Strong"
    }

    val icon = when (strength) {
        PasswordStrength.EMPTY -> null
        PasswordStrength.WEAK -> null
        PasswordStrength.MEDIUM -> null
        PasswordStrength.STRONG -> Icons.Default.Check
    }

    Column {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = CardBorder.copy(alpha = 0.3f)
        )

        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Password strength: $label",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = color
                    )
                }
            }
        }
    }
}

// Extension function for email validation
private fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
