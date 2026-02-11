package com.pyera.app.ui.security

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import com.pyera.app.ui.util.pyeraBackground
import kotlinx.coroutines.delay

/**
 * App Lock Screen - PIN entry with biometric option with rate limiting
 */
@Composable
fun AppLockScreen(
    onUnlockSuccess: () -> Unit,
    onForgotPin: () -> Unit = {},
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var pin by rememberSaveable { mutableStateOf("") }
    
    // Lockout state
    var isLockedOut by remember { mutableStateOf(viewModel.isLockedOut()) }
    var remainingLockoutTime by remember { mutableLongStateOf(viewModel.getRemainingLockoutTime()) }
    var remainingAttempts by remember { mutableStateOf(viewModel.getRemainingAttempts()) }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SecurityEvent.UnlockSuccess -> {
                    onUnlockSuccess()
                }
                else -> {}
            }
        }
    }
    
    // Update lockout status periodically
    LaunchedEffect(isLockedOut) {
        while (isLockedOut) {
            remainingLockoutTime = viewModel.getRemainingLockoutTime()
            isLockedOut = viewModel.isLockedOut()
            if (isLockedOut) {
                delay(1000) // Update every second
            }
        }
    }
    
    // Try biometric on first launch if enabled and not locked out
    LaunchedEffect(Unit) {
        if (!isLockedOut && viewModel.canUseBiometricForUnlock() && context is FragmentActivity) {
            viewModel.unlockWithBiometric(context)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pyeraBackground()
            .padding(SpacingTokens.MediumLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Lock Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(ColorTokens.SurfaceLevel2, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isLockedOut) MaterialTheme.colorScheme.error else ColorTokens.Primary500
            )
        }
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
        
        // Title
        Text(
            text = if (isLockedOut) "Locked Out" else "Enter PIN",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Small))
        
        // Subtitle / Lockout message
        if (isLockedOut) {
            Text(
                text = "Too many failed attempts\nTry again in ${formatLockoutTime(remainingLockoutTime)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Unlock Pyera to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
        
        // PIN Dots (only show if not locked out)
        if (!isLockedOut) {
            PinDots(
                pinLength = pin.length,
                maxLength = 6,
                shake = uiState.shakePin,
                error = uiState.pinError != null
            )
            
            // Error message
            if (uiState.pinError != null) {
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                Text(
                    text = uiState.pinError!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                // Update remaining attempts after error
                LaunchedEffect(uiState.pinError) {
                    remainingAttempts = viewModel.getRemainingAttempts()
                }
            }
            
            // Remaining attempts warning
            if (uiState.pinError == null && remainingAttempts in 1..2) {
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                Text(
                    text = "$remainingAttempts attempts remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Biometric button (if available and not locked out)
        if (!isLockedOut && uiState.canUseBiometric && uiState.isBiometricEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (context is FragmentActivity) {
                            viewModel.unlockWithBiometric(context)
                        }
                    }
                    .padding(SpacingTokens.MediumSmall),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Use Biometric",
                    modifier = Modifier.size(48.dp),
                    tint = ColorTokens.Primary500
                )
            }
            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))
        }
        
        // PIN Pad (disabled when locked out)
        PinPad(
            enabled = !isLockedOut,
            onNumberClick = { number ->
                if (pin.length < 6) {
                    pin += number
                    viewModel.clearErrors()
                    
                    if (pin.length >= 4) {
                        val success = viewModel.verifyPin(pin)
                        if (success) {
                            onUnlockSuccess()
                        } else {
                            // Update lockout state
                            isLockedOut = viewModel.isLockedOut()
                            remainingLockoutTime = viewModel.getRemainingLockoutTime()
                            remainingAttempts = viewModel.getRemainingAttempts()
                            pin = ""
                        }
                    }
                }
            },
            onBackspaceClick = {
                if (pin.isNotEmpty()) {
                    pin = pin.dropLast(1)
                    viewModel.clearErrors()
                }
            },
            onForgotClick = onForgotPin
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
    }
}

/**
 * Format lockout time in human-readable format
 */
private fun formatLockoutTime(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    
    return when {
        minutes > 0 -> "${minutes}m ${remainingSeconds}s"
        else -> "${remainingSeconds}s"
    }
}

@Composable
private fun PinDots(
    pinLength: Int,
    maxLength: Int,
    shake: Boolean,
    error: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (shake) 1.1f else 1f,
        label = "shake"
    )
    
    Row(
        modifier = Modifier.scale(scale),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)
    ) {
        repeat(maxLength) { index ->
            Box(
                modifier = Modifier
                    .size(SpacingTokens.Medium)
                    .background(
                        color = when {
                            error -> MaterialTheme.colorScheme.error
                            index < pinLength -> ColorTokens.Primary500
                            else -> ColorTokens.SurfaceLevel2
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PinPad(
    enabled: Boolean = true,
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onForgotClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)
    ) {
        // Rows 1-3 (1-9)
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..2) {
                    val number = (row * 3 + col + 1).toString()
                    PinButton(
                        text = number,
                        enabled = enabled,
                        onClick = { onNumberClick(number) }
                    )
                }
            }
        }
        
        // Row 4 (Forgot, 0, Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Forgot PIN
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clickable(enabled = enabled, onClick = onForgotClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Forgot?",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant 
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
            
            // 0
            PinButton(
                text = "0",
                enabled = enabled,
                onClick = { onNumberClick("0") }
            )
            
            // Backspace
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clickable(enabled = enabled, onClick = onBackspaceClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(28.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.onBackground 
                           else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f)
                )
            }
        }
    }
}

@Composable
private fun PinButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(
                if (enabled) ColorTokens.SurfaceLevel2 else ColorTokens.SurfaceLevel2.copy(alpha = 0.38f), 
                CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            color = if (enabled) MaterialTheme.colorScheme.onBackground 
                   else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
            fontSize = 28.sp
        )
    }
}



