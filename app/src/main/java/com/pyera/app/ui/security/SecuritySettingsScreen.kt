package com.pyera.app.ui.security

import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.ui.components.PyeraButton
import com.pyera.app.ui.util.pyeraBackground

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
                title = { Text("Security", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
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
                        color = ColorTokens.SurfaceLevel2,
                        modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge)
                    )
                    
                    // Immediate Lock Button
                    PyeraButton(
                        onClick = { viewModel.lockNow() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.MediumLarge)
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
            
            Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))
        }
    }
    
    // Timeout Selection Dialog
    if (showTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            title = { Text("Lock Timeout", color = MaterialTheme.colorScheme.onBackground) },
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
                                ColorTokens.Primary500 else MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTimeoutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = ColorTokens.SurfaceLevel2
        )
    }
    
    // Disable App Lock Confirmation Dialog
    if (showDisableDialog) {
        AlertDialog(
            onDismissRequest = { showDisableDialog = false },
            title = { Text("Disable App Lock?", color = MaterialTheme.colorScheme.onBackground) },
            text = { 
                Text(
                    "This will remove your PIN and biometric settings. Are you sure?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = ColorTokens.SurfaceLevel2
        )
    }
    
    // Biometric Error Dialog
    if (showBiometricError != null || uiState.biometricError != null) {
        AlertDialog(
            onDismissRequest = { 
                showBiometricError = null
                viewModel.clearErrors()
            },
            title = { Text("Biometric Unavailable", color = MaterialTheme.colorScheme.onBackground) },
            text = { 
                Text(
                    uiState.biometricError ?: showBiometricError ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showBiometricError = null
                        viewModel.clearErrors()
                    }
                ) {
                    Text("OK", color = ColorTokens.Primary500)
                }
            },
            containerColor = ColorTokens.SurfaceLevel2
        )
    }
}

@Composable
private fun SecuritySettingsSection(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = SpacingTokens.Small)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = ColorTokens.Primary500,
            modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge, vertical = SpacingTokens.Small)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.MediumLarge)
                .background(ColorTokens.SurfaceLevel2, MaterialTheme.shapes.medium)
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
            .padding(SpacingTokens.MediumLarge),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            .padding(SpacingTokens.MediumLarge),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ColorTokens.Primary500,
                checkedTrackColor = ColorTokens.Primary500.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = ColorTokens.SurfaceLevel2
            )
        )
    }
}






