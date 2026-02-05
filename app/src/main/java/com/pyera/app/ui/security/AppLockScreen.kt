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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.pyera.app.ui.theme.DarkGreen
import com.pyera.app.ui.theme.NeonYellow
import com.pyera.app.ui.theme.Spacing
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary

/**
 * App Lock Screen - PIN entry with biometric option
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
    
    // Try biometric on first launch if enabled
    LaunchedEffect(Unit) {
        if (viewModel.canUseBiometricForUnlock() && context is FragmentActivity) {
            viewModel.unlockWithBiometric(context)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen)
            .padding(Spacing.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Lock Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(SurfaceElevated, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = NeonYellow
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.Large))
        
        // Title
        Text(
            text = "Enter PIN",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(Spacing.Small))
        
        // Subtitle
        Text(
            text = "Unlock Pyera to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(Spacing.XLarge))
        
        // PIN Dots
        PinDots(
            pinLength = pin.length,
            maxLength = 6,
            shake = uiState.shakePin,
            error = uiState.pinError != null
        )
        
        // Error message
        if (uiState.pinError != null) {
            Spacer(modifier = Modifier.height(Spacing.Small))
            Text(
                text = uiState.pinError!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Biometric button (if available)
        if (uiState.canUseBiometric && uiState.isBiometricEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (context is FragmentActivity) {
                            viewModel.unlockWithBiometric(context)
                        }
                    }
                    .padding(Spacing.Medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Use Biometric",
                    modifier = Modifier.size(48.dp),
                    tint = NeonYellow
                )
            }
            Spacer(modifier = Modifier.height(Spacing.Medium))
        }
        
        // PIN Pad
        PinPad(
            onNumberClick = { number ->
                if (pin.length < 6) {
                    pin += number
                    viewModel.clearErrors()
                    
                    if (pin.length >= 4) {
                        viewModel.verifyPin(pin)
                        if (uiState.pinError != null) {
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
        
        Spacer(modifier = Modifier.height(Spacing.Large))
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
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(maxLength) { index ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = when {
                            error -> MaterialTheme.colorScheme.error
                            index < pinLength -> NeonYellow
                            else -> SurfaceElevated
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PinPad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onForgotClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    .clickable(onClick = onForgotClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Forgot?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            // 0
            PinButton(
                text = "0",
                onClick = { onNumberClick("0") }
            )
            
            // Backspace
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clickable(onClick = onBackspaceClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(28.dp),
                    tint = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun PinButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(SurfaceElevated, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontSize = 28.sp
        )
    }
}
