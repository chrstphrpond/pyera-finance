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
import androidx.compose.material.icons.automirrored.filled.Backspace
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.ui.components.PyeraButton
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import com.pyera.app.ui.util.pyeraBackground

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
    
    var step by rememberSaveable { mutableStateOf(PinSetupStep.ENTER_PIN) }
    var firstPin by rememberSaveable { mutableStateOf("") }
    var secondPin by rememberSaveable { mutableStateOf("") }
    var enableBiometric by rememberSaveable { mutableStateOf(false) }
    
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
            .pyeraBackground()
            .padding(SpacingTokens.MediumLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Small))
        
        // Subtitle
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
        
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
                    Spacer(modifier = Modifier.height(SpacingTokens.Small))
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
                
                Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))
                
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
                
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                
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
                        .background(ColorTokens.SurfaceLevel2, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = ColorTokens.Primary500
                    )
                }
                
                Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
                
                // Biometric toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorTokens.SurfaceLevel2, CircleShape)
                        .padding(SpacingTokens.MediumSmall)
                        .clickable { enableBiometric = !enableBiometric },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Use Biometric",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(
                        checked = enableBiometric,
                        onCheckedChange = { enableBiometric = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ColorTokens.Primary500,
                            checkedTrackColor = ColorTokens.Primary500.copy(alpha = 0.5f)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
                
                // Buttons
                PyeraButton(
                    onClick = {
                        viewModel.setPin(firstPin, enableBiometric)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Complete Setup")
                }
                
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                
                PyeraButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip")
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
    }
}

@Composable
private fun PinSetupDots(
    pinLength: Int,
    maxLength: Int,
    error: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)
    ) {
        repeat(maxLength) { index ->
            Box(
                modifier = Modifier
                    .size(20.dp)
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
private fun PinSetupPad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit
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
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onBackground
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
            .background(ColorTokens.SurfaceLevel2, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp
        )
    }
}

private enum class PinSetupStep {
    ENTER_PIN,
    CONFIRM_PIN,
    ENABLE_BIOMETRIC
}



