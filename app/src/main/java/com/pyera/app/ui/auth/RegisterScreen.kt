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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.R
import com.pyera.app.ui.components.PyeraAuthButton
import com.pyera.app.ui.components.PyeraAuthTextField
import com.pyera.app.ui.components.PyeraPasswordField
import com.pyera.app.ui.components.PasswordStrength
import com.pyera.app.ui.components.calculatePasswordStrength
import androidx.compose.ui.res.stringResource
import com.pyera.app.R
import com.pyera.app.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState = uiState.authState

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    
    // Field-level validation errors
    var fieldErrors by rememberSaveable { mutableStateOf(RegisterValidationErrors()) }

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
            Spacer(modifier = Modifier.height(40.dp))

            // Top Section - Logo and Branding
            RegisterHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // Registration Form Card with elevated surface
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, ColorBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                // Create Account Text
                Text(
                    text = stringResource(R.string.auth_register_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(R.string.auth_register_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Full Name Field
                PyeraAuthTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        fieldErrors = fieldErrors.copy(nameError = null)
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = stringResource(R.string.auth_register_name_label),
                    placeholder = stringResource(R.string.auth_register_name_placeholder),
                    leadingIcon = Icons.Default.Person,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    isError = fieldErrors.nameError != null,
                    errorMessage = fieldErrors.nameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Field
                PyeraAuthTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        fieldErrors = fieldErrors.copy(emailError = null)
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = stringResource(R.string.auth_register_email_label),
                    placeholder = stringResource(R.string.auth_register_email_placeholder),
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    isError = fieldErrors.emailError != null,
                    errorMessage = fieldErrors.emailError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field with strength indicator
                PyeraPasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        fieldErrors = fieldErrors.copy(passwordError = null)
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = stringResource(R.string.auth_register_password_label),
                    placeholder = stringResource(R.string.auth_register_password_placeholder),
                    isPasswordVisible = uiState.isPasswordVisible,
                    onPasswordVisibilityChange = { viewModel.togglePasswordVisibility() },
                    imeAction = ImeAction.Next,
                    isError = fieldErrors.passwordError != null,
                    errorMessage = fieldErrors.passwordError,
                    showStrengthIndicator = true,
                    passwordStrength = passwordStrength
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Field with matching indicator
                PyeraAuthTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        fieldErrors = fieldErrors.copy(confirmPasswordError = null)
                        if (authState is AuthState.Error) viewModel.clearError()
                    },
                    label = stringResource(R.string.auth_register_confirm_password_label),
                    placeholder = stringResource(R.string.auth_register_confirm_password_placeholder),
                    isPassword = true,
                    isPasswordVisible = uiState.isConfirmPasswordVisible,
                    onPasswordVisibilityChange = { viewModel.toggleConfirmPasswordVisibility() },
                    leadingIcon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isError = fieldErrors.confirmPasswordError != null || (!passwordsMatch && confirmPassword.isNotEmpty()),
                    errorMessage = fieldErrors.confirmPasswordError ?: if (!passwordsMatch && confirmPassword.isNotEmpty()) "Passwords do not match" else null,
                    onImeAction = {
                        if (isFormValid) {
                            focusManager.clearFocus()
                            val errors = viewModel.validateRegisterFields(
                                name, email, password, confirmPassword, uiState.termsAccepted
                            )
                            fieldErrors = errors
                            if (errors.nameError == null && errors.emailError == null && 
                                errors.passwordError == null && errors.confirmPasswordError == null && errors.termsError == null) {
                                viewModel.register(email, password, name)
                            }
                        }
                    }
                )

                // Password match indicator
                AnimatedVisibility(
                    visible = confirmPassword.isNotEmpty() && passwordsMatch,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.auth_register_passwords_match_content_desc),
                            modifier = Modifier.size(16.dp),
                            tint = ColorSuccess
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.auth_register_error_passwords_match),
                            style = MaterialTheme.typography.labelSmall,
                            color = ColorSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Terms & Conditions Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.termsAccepted,
                        onCheckedChange = { 
                            viewModel.toggleTermsAccepted()
                            fieldErrors = fieldErrors.copy(termsError = null)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentGreen,
                            uncheckedColor = if (fieldErrors.termsError != null) ColorError else TextTertiary,
                            checkmarkColor = DeepBackground
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.auth_register_terms_prefix),
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
                                color = AccentGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Terms error message
                AnimatedVisibility(
                    visible = fieldErrors.termsError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = fieldErrors.termsError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorError,
                        modifier = Modifier.padding(start = 48.dp, top = 4.dp)
                    )
                }

                // General Error Message
                AnimatedVisibility(
                    visible = authState is AuthState.Error,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (authState is AuthState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorError,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Create Account Button with loading state
                PyeraAuthButton(
                    text = stringResource(R.string.auth_register_button),
                    onClick = {
                        // Validate all fields before submitting
                        val errors = viewModel.validateRegisterFields(
                            name, email, password, confirmPassword, uiState.termsAccepted
                        )
                        fieldErrors = errors
                        
                        if (errors.nameError == null && errors.emailError == null && 
                            errors.passwordError == null && errors.confirmPasswordError == null && errors.termsError == null) {
                            focusManager.clearFocus()
                            viewModel.register(email, password, name)
                        }
                    },
                    isLoading = authState is AuthState.Loading,
                    enabled = authState !is AuthState.Loading
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.auth_register_has_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = stringResource(R.string.auth_register_sign_in),
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
    }
}

@Composable
private fun RegisterHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo
        Image(
            painter = painterResource(id = R.drawable.ic_logo_full),
            contentDescription = stringResource(R.string.auth_login_logo_content_desc),
            modifier = Modifier
                .height(50.dp)
                .wrapContentWidth(),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = stringResource(R.string.auth_register_header_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// Extension function for email validation
private fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
