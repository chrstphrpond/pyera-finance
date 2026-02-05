package com.pyera.app.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.ui.components.PyeraButton
import com.pyera.app.ui.theme.ColorBorder
import com.pyera.app.ui.theme.DarkGreen
import com.pyera.app.ui.theme.NeonYellow
import com.pyera.app.ui.theme.Spacing
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary

/**
 * Screen for setting up a new PIN
 */
@Composable
fun SetPinScreen(
    onPinSet: () -> Unit,
    onCancel: () -> Unit = {},
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var step by remember { mutableStateOf(PinSetupStep.ENTER_PIN) }
    var firstPin by remember { mutableStateOf("") }
    var secondPin by remember { mutableStateOf("") }
    var enableBiometric by remember { mutableStateOf(false) }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SecurityEvent.PinSetSuccess -> {
                    onPinSet()
                }
                else -> {}
            }
        }
    }
    
    val currentPin = when (step) {
        PinSetupStep.ENTER_PIN -> firstPin
        PinSetupStep.CONFIRM_PIN -> secondPin
        PinSetupStep.ENABLE_BIOMETRIC -> firstPin
    }
    
    val title = when (step) {
        PinSetupStep.ENTER_PIN -> "Create PIN"
        PinSetupStep.CONFIRM_PIN -> "Confirm PIN"
        PinSetupStep.ENABLE_BIOMETRIC -> "Enable Biometric?"
    }
    
    val subtitle = when (step) {
        PinSetupStep.ENTER_PIN -> "Enter a 4-6 digit PIN to secure your app"
        PinSetupStep.CONFIRM_PIN -> "Re-enter your PIN to confirm"
        PinSetupStep.ENABLE_BIOMETRIC -> "Use your fingerprint or face to unlock"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen)
            .padding(Spacing.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(Spacing.Small))
        
        // Subtitle
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.XLarge))
        
        when (step) {
            PinSetupStep.ENTER_PIN, PinSetupStep.CONFIRM_PIN -> {
                // PIN Dots
                PinSetupDots(
                    pinLength = currentPin.length,
                    maxLength = 6,
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
                
                // PIN Pad
                PinSetupPad(
                    onNumberClick = { number ->
                        if (currentPin.length < 6) {
                            when (step) {
                                PinSetupStep.ENTER_PIN -> {
                                    firstPin += number
                                    viewModel.clearErrors()
                                }
                                PinSetupStep.CONFIRM_PIN -> {
                                    secondPin += number
                                    viewModel.clearErrors()
                                }
                                else -> {}
                            }
                        }
                    },
                    onBackspaceClick = {
                        when (step) {
                            PinSetupStep.ENTER_PIN -> {
                                if (firstPin.isNotEmpty()) {
                                    firstPin = firstPin.dropLast(1)
                                    viewModel.clearErrors()
                                }
                            }
                            PinSetupStep.CONFIRM_PIN -> {
                                if (secondPin.isNotEmpty()) {
                                    secondPin = secondPin.dropLast(1)
                                    viewModel.clearErrors()
                                }
                            }
                            else -> {}
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(Spacing.Medium))
                
                // Continue/Cancel buttons
                if (currentPin.length >= 4) {
                    PyeraButton(
                        onClick = {
                            when (step) {
                                PinSetupStep.ENTER_PIN -> {
                                    step = PinSetupStep.CONFIRM_PIN
                                }
                                PinSetupStep.CONFIRM_PIN -> {
                                    if (firstPin == secondPin) {
                                        if (uiState.canUseBiometric) {
                                            step = PinSetupStep.ENABLE_BIOMETRIC
                                        } else {
                                            viewModel.setPin(firstPin, false)
                                        }
                                    } else {
                                        viewModel.clearErrors()
                                        secondPin = ""
                                        // Show error for mismatch
                                    }
                                }
                                else -> {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue")
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.Small))
                
                PyeraButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
            
            PinSetupStep.ENABLE_BIOMETRIC -> {
                Spacer(modifier = Modifier.weight(1f))
                
                // Biometric Icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(SurfaceElevated, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = NeonYellow
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.XLarge))
                
                // Biometric toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceElevated, CircleShape)
                        .padding(Spacing.Medium)
                        .clickable { enableBiometric = !enableBiometric },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Use Biometric",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Switch(
                        checked = enableBiometric,
                        onCheckedChange = { enableBiometric = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonYellow,
                            checkedTrackColor = NeonYellow.copy(alpha = 0.5f)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.XLarge))
                
                // Buttons
                PyeraButton(
                    onClick = {
                        viewModel.setPin(firstPin, enableBiometric)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Complete Setup")
                }
                
                Spacer(modifier = Modifier.height(Spacing.Small))
                
                PyeraButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip")
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.Large))
    }
}

@Composable
private fun PinSetupDots(
    pinLength: Int,
    maxLength: Int,
    error: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(maxLength) { index ->
            Box(
                modifier = Modifier
                    .size(20.dp)
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
private fun PinSetupPad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit
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
                    PinSetupButton(
                        text = number,
                        onClick = { onNumberClick(number) }
                    )
                }
            }
        }
        
        // Row 4 (Empty, 0, Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Empty space
            Box(modifier = Modifier.size(72.dp))
            
            // 0
            PinSetupButton(
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
private fun PinSetupButton(
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

private enum class PinSetupStep {
    ENTER_PIN,
    CONFIRM_PIN,
    ENABLE_BIOMETRIC
}
