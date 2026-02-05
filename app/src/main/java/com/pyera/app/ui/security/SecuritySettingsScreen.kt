package com.pyera.app.ui.security

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
 * Security Settings Screen - Main settings for app lock
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSetPin: () -> Unit,
    onNavigateToChangePin: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showTimeoutDialog by rememberSaveable { mutableStateOf(false) }
    var showDisableDialog by rememberSaveable { mutableStateOf(false) }
    var showBiometricError by rememberSaveable { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen
                )
            )
        },
        containerColor = DarkGreen
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // App Lock Toggle
            SecuritySettingsSection(title = "App Lock") {
                SecurityToggleItem(
                    icon = Icons.Default.Lock,
                    title = "Enable App Lock",
                    subtitle = if (uiState.isAppLockEnabled) "PIN required to open app" else "No PIN required",
                    checked = uiState.isAppLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (!uiState.isPinSet) {
                                onNavigateToSetPin()
                            } else {
                                viewModel.toggleAppLock(true)
                            }
                        } else {
                            showDisableDialog = true
                        }
                    }
                )
            }
            
            // Only show these if app lock is enabled
            if (uiState.isAppLockEnabled) {
                // PIN Settings
                SecuritySettingsSection(title = "PIN") {
                    SecuritySettingsItem(
                        icon = Icons.Default.LockOpen,
                        title = "Change PIN",
                        subtitle = "Update your current PIN",
                        onClick = onNavigateToChangePin
                    )
                }
                
                // Biometric
                if (uiState.canUseBiometric) {
                    SecuritySettingsSection(title = "Biometric") {
                        SecurityToggleItem(
                            icon = Icons.Default.Fingerprint,
                            title = "Use Biometric",
                            subtitle = "Unlock with fingerprint or face",
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.toggleBiometric(enabled)
                            }
                        )
                    }
                }
                
                // Auto-lock Settings
                SecuritySettingsSection(title = "Auto-Lock") {
                    SecuritySettingsItem(
                        icon = Icons.Default.Timer,
                        title = "Lock Timeout",
                        subtitle = viewModel.getTimeoutDisplayText(uiState.lockTimeout),
                        onClick = { showTimeoutDialog = true }
                    )
                    
                    HorizontalDivider(
                        color = SurfaceElevated,
                        modifier = Modifier.padding(horizontal = Spacing.CardPadding)
                    )
                    
                    // Immediate Lock Button
                    PyeraButton(
                        onClick = { viewModel.lockNow() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.CardPadding)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockClock,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Lock App Now")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.XXXLarge))
        }
    }
    
    // Timeout Selection Dialog
    if (showTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            title = { Text("Lock Timeout", color = TextPrimary) },
            text = {
                Column {
                    uiState.timeoutOptions.forEach { option ->
                        Text(
                            text = option.displayText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLockTimeout(option.millis)
                                    showTimeoutDialog = false
                                }
                                .padding(vertical = 12.dp),
                            color = if (option.millis == uiState.lockTimeout) 
                                NeonYellow else TextPrimary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTimeoutDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceElevated
        )
    }
    
    // Disable App Lock Confirmation Dialog
    if (showDisableDialog) {
        AlertDialog(
            onDismissRequest = { showDisableDialog = false },
            title = { Text("Disable App Lock?", color = TextPrimary) },
            text = { 
                Text(
                    "This will remove your PIN and biometric settings. Are you sure?",
                    color = TextSecondary
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.toggleAppLock(false)
                        showDisableDialog = false
                    }
                ) {
                    Text("Disable", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceElevated
        )
    }
    
    // Biometric Error Dialog
    if (showBiometricError != null || uiState.biometricError != null) {
        AlertDialog(
            onDismissRequest = { 
                showBiometricError = null
                viewModel.clearErrors()
            },
            title = { Text("Biometric Unavailable", color = TextPrimary) },
            text = { 
                Text(
                    uiState.biometricError ?: showBiometricError ?: "",
                    color = TextSecondary
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showBiometricError = null
                        viewModel.clearErrors()
                    }
                ) {
                    Text("OK", color = NeonYellow)
                }
            },
            containerColor = SurfaceElevated
        )
    }
}

@Composable
private fun SecuritySettingsSection(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = Spacing.Small)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = NeonYellow,
            modifier = Modifier.padding(horizontal = Spacing.ScreenPadding, vertical = Spacing.Small)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenPadding)
                .background(SurfaceElevated, MaterialTheme.shapes.medium)
        ) {
            content()
        }
    }
}

@Composable
private fun SecuritySettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Spacing.CardPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}

@Composable
private fun SecurityToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.CardPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonYellow,
                checkedTrackColor = NeonYellow.copy(alpha = 0.5f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceElevated
            )
        )
    }
}


