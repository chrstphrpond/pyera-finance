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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.ui.components.PyeraButton
import com.pyera.app.ui.theme.DarkGreen
import com.pyera.app.ui.theme.NeonYellow
import com.pyera.app.ui.theme.Spacing
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary

/**
 * Screen for changing the existing PIN
 */
@Composable
fun ChangePinScreen(
    onPinChanged: () -> Unit,
    onCancel: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var step by remember { mutableStateOf(ChangePinStep.CURRENT_PIN) }
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SecurityEvent.PinChangedSuccess -> {
                    onPinChanged()
                }
                else -> {}
            }
        }
    }
    
    val pinValue = when (step) {
        ChangePinStep.CURRENT_PIN -> currentPin
        ChangePinStep.NEW_PIN -> newPin
        ChangePinStep.CONFIRM_PIN -> confirmPin
    }
    
    val title = when (step) {
        ChangePinStep.CURRENT_PIN -> "Enter Current PIN"
        ChangePinStep.NEW_PIN -> "Enter New PIN"
        ChangePinStep.CONFIRM_PIN -> "Confirm New PIN"
    }
    
    val subtitle = when (step) {
        ChangePinStep.CURRENT_PIN -> "Enter your current PIN to continue"
        ChangePinStep.NEW_PIN -> "Create a new 4-6 digit PIN"
        ChangePinStep.CONFIRM_PIN -> "Re-enter your new PIN"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen)
            .padding(Spacing.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
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
        
        // PIN Dots
        ChangePinDots(
            pinLength = pinValue.length,
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
        ChangePinPad(
            onNumberClick = { number ->
                if (pinValue.length < 6) {
                    when (step) {
                        ChangePinStep.CURRENT_PIN -> {
                            currentPin += number
                            viewModel.clearErrors()
                        }
                        ChangePinStep.NEW_PIN -> {
                            newPin += number
                            viewModel.clearErrors()
                        }
                        ChangePinStep.CONFIRM_PIN -> {
                            confirmPin += number
                            viewModel.clearErrors()
                        }
                    }
                }
            },
            onBackspaceClick = {
                when (step) {
                    ChangePinStep.CURRENT_PIN -> {
                        if (currentPin.isNotEmpty()) {
                            currentPin = currentPin.dropLast(1)
                            viewModel.clearErrors()
                        }
                    }
                    ChangePinStep.NEW_PIN -> {
                        if (newPin.isNotEmpty()) {
                            newPin = newPin.dropLast(1)
                            viewModel.clearErrors()
                        }
                    }
                    ChangePinStep.CONFIRM_PIN -> {
                        if (confirmPin.isNotEmpty()) {
                            confirmPin = confirmPin.dropLast(1)
                            viewModel.clearErrors()
                        }
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(Spacing.Medium))
        
        // Continue button
        if (pinValue.length >= 4) {
            PyeraButton(
                onClick = {
                    when (step) {
                        ChangePinStep.CURRENT_PIN -> {
                            // Verify current PIN
                            if (viewModel.verifyPin(currentPin)) {
                                step = ChangePinStep.NEW_PIN
                                currentPin = "" // Clear for security
                            }
                        }
                        ChangePinStep.NEW_PIN -> {
                            step = ChangePinStep.CONFIRM_PIN
                        }
                        ChangePinStep.CONFIRM_PIN -> {
                            if (newPin == confirmPin) {
                                viewModel.changePin(currentPin, newPin)
                            } else {
                                viewModel.clearErrors()
                                confirmPin = ""
                                // Show error
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when (step) {
                        ChangePinStep.CURRENT_PIN -> "Verify"
                        ChangePinStep.NEW_PIN -> "Continue"
                        ChangePinStep.CONFIRM_PIN -> "Change PIN"
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.Small))
        
        PyeraButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
        
        Spacer(modifier = Modifier.height(Spacing.Large))
    }
}

@Composable
private fun ChangePinDots(
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
private fun ChangePinPad(
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
                    ChangePinButton(
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
            ChangePinButton(
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
private fun ChangePinButton(
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

private enum class ChangePinStep {
    CURRENT_PIN,
    NEW_PIN,
    CONFIRM_PIN
}
