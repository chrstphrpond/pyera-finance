package com.pyera.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.ColorBorder
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DeepBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            ProfileHeader(
                userName = state.userName,
                email = state.email,
                avatarInitials = state.avatarInitials,
                onEditProfile = { /* Navigate to edit profile */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            StatsRow(
                totalTransactions = state.totalTransactions,
                totalSavings = state.totalSavings,
                budgetStatus = state.budgetStatus
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Section
            SettingsSection(
                notificationsEnabled = state.notificationsEnabled,
                currency = state.currency,
                appearance = state.appearance,
                isBiometricAvailable = state.isBiometricAvailable,
                isBiometricEnabled = state.isBiometricEnabled,
                onToggleNotifications = { enabled ->
                    viewModel.onEvent(ProfileEvent.ToggleNotifications(enabled))
                },
                onToggleBiometric = { enabled ->
                    viewModel.onEvent(ProfileEvent.ToggleBiometric(enabled))
                },
                onAccountSettingsClick = {
                    viewModel.onEvent(ProfileEvent.NavigateToAccountSettings)
                },
                onDataPrivacyClick = {
                    viewModel.onEvent(ProfileEvent.NavigateToDataPrivacy)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Actions Section
            ActionsSection(
                onExportDataClick = {
                    viewModel.onEvent(ProfileEvent.ExportData)
                },
                onHelpSupportClick = {
                    viewModel.onEvent(ProfileEvent.NavigateToHelpSupport)
                },
                onAboutClick = {
                    viewModel.onEvent(ProfileEvent.NavigateToAbout)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Danger Zone
            DangerZone(
                onLogoutClick = {
                    viewModel.onEvent(ProfileEvent.Logout)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Footer(appVersion = state.appVersion)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    email: String,
    avatarInitials: String,
    onEditProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarInitials,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Name
        Text(
            text = userName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Email
        Text(
            text = email,
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Edit Profile Button
        OutlinedButton(
            onClick = onEditProfile,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AccentGreen
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(ColorBorder)
            )
        ) {
            Text(
                text = "Edit Profile",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StatsRow(
    totalTransactions: Int,
    totalSavings: Double,
    budgetStatus: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Transactions",
            value = totalTransactions.toString(),
            icon = Icons.Default.ReceiptLong
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Savings",
            value = "₱${String.format("%.0f", totalSavings)}",
            icon = Icons.Default.Savings
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Budget",
            value = budgetStatus,
            icon = Icons.Default.AccountBalanceWallet
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector
) {
    PyeraCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SettingsSection(
    notificationsEnabled: Boolean,
    currency: String,
    appearance: String,
    isBiometricAvailable: Boolean,
    isBiometricEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onAccountSettingsClick: () -> Unit,
    onDataPrivacyClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Section Header
        Text(
            text = "Settings",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        PyeraCard {
            Column {
                // Account Settings
                MenuItemWithArrow(
                    icon = Icons.Default.Person,
                    title = "Account Settings",
                    onClick = onAccountSettingsClick
                )

                Divider(color = ColorBorder, thickness = 1.dp)

                // Notifications
                MenuItemWithToggle(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = onToggleNotifications
                )

                Divider(color = ColorBorder, thickness = 1.dp)

                // Biometric Authentication (only show if available)
                if (isBiometricAvailable) {
                    MenuItemWithToggle(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Login",
                        checked = isBiometricEnabled,
                        onCheckedChange = onToggleBiometric
                    )

                    Divider(color = ColorBorder, thickness = 1.dp)
                }

                // Currency
                MenuItemWithValue(
                    icon = Icons.Default.AttachMoney,
                    title = "Currency",
                    value = currency
                )

                Divider(color = ColorBorder, thickness = 1.dp)

                // Appearance
                MenuItemWithValue(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    value = appearance
                )

                Divider(color = ColorBorder, thickness = 1.dp)

                // Data & Privacy
                MenuItemWithArrow(
                    icon = Icons.Default.Shield,
                    title = "Data & Privacy",
                    onClick = onDataPrivacyClick
                )
            }
        }
    }
}

@Composable
fun ActionsSection(
    onExportDataClick: () -> Unit,
    onHelpSupportClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Section Header
        Text(
            text = "Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        PyeraCard {
            Column {
                // Export Data
                MenuItemWithArrow(
                    icon = Icons.Default.Download,
                    title = "Export Data",
                    onClick = onExportDataClick
                )

                Divider(color = ColorBorder, thickness = 1.dp)

                // Help & Support
                MenuItemWithArrow(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    onClick = onHelpSupportClick
                )

                Divider(color = ColorBorder, thickness = 1.dp)

                // About Pyera
                MenuItemWithArrow(
                    icon = Icons.Default.Info,
                    title = "About Pyera",
                    onClick = onAboutClick
                )
            }
        }
    }
}

@Composable
fun MenuItemWithArrow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun MenuItemWithToggle(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentGreen,
                checkedTrackColor = AccentGreen.copy(alpha = 0.5f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = ColorBorder
            )
        )
    }
}

@Composable
fun MenuItemWithValue(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun DangerZone(
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ColorError
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(ColorError)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = ColorError
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Out",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun Footer(
    appVersion: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pyera v$appVersion",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "© 2024 Pyera Finance. All rights reserved.",
            fontSize = 10.sp,
            color = TextSecondary.copy(alpha = 0.7f)
        )
    }
}
