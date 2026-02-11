package com.pyera.app.ui.profile

import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.core.content.FileProvider
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.pyera.app.ui.components.PyeraButton
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.components.ButtonVariant
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.util.pyeraBackground
import java.io.File

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ExportReady -> {
                    try {
                        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                        val exportFile = File(exportDir, event.fileName)
                        exportFile.writeText(event.csvContent)

                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            exportFile
                        )

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        context.startActivity(
                            Intent.createChooser(shareIntent, "Export CSV")
                        )
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Export failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                is ProfileEvent.ExportFailed -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // User Header
            UserProfileHeader(
                userName = state.userName.ifEmpty { "User" },
                email = state.email.ifEmpty { "user@example.com" },
                avatarUrl = state.avatarUrl
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Quick Stats
            QuickStatsRow(
                transactionCount = state.transactionCount,
                savingsGoals = state.savingsGoalsCount,
                activeBudgets = state.activeBudgetsCount
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Settings Sections
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Accounts",
                    subtitle = "Manage your wallets & banks",
                    onClick = { navController.navigate(Screen.Accounts.route) }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Personal Information",
                    onClick = { /* navController.navigate(Screen.EditProfile.route) */ }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Security",
                    onClick = { navController.navigate(Screen.SecuritySettings.route) }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    trailing = {
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = viewModel::setNotificationsEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ColorTokens.Primary500,
                                checkedTrackColor = ColorTokens.Primary500.copy(alpha = 0.5f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = ColorBorder
                            )
                        )
                    }
                )
            }

            SettingsSection(title = "Data") {
                SettingsItem(
                    icon = Icons.Default.Rule,
                    title = "Transaction Rules",
                    subtitle = "Auto-categorization",
                    onClick = { navController.navigate(Screen.TransactionRules.route) }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.ContentCopy,
                    title = "Transaction Templates",
                    subtitle = "Quick transaction entry",
                    onClick = { navController.navigate(Screen.Templates.List.route) }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Download,
                    title = "Export to CSV",
                    onClick = { viewModel.exportData() }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "Backup & Restore",
                    onClick = { /* navController.navigate(Screen.Backup.route) */ }
                )
            }

            SettingsSection(title = "App") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    subtitle = "Dark mode",
                    onClick = { /* navController.navigate(Screen.Appearance.route) */ }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    onClick = { /* navController.navigate(Screen.Support.route) */ }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Version ${state.appVersion}",
                    onClick = { /* navController.navigate(Screen.About.route) */ }
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Logout Button
            PyeraButton(
                onClick = { viewModel.logout() },
                variant = ButtonVariant.Destructive,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.MediumLarge)
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))
        }
    }
}

@Composable
private fun UserProfileHeader(
    userName: String,
    email: String,
    avatarUrl: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpacingTokens.MediumLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = ColorTokens.SurfaceLevel2,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = ColorTokens.Primary500
                )
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

        // Name
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Email
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickStatsRow(
    transactionCount: Int,
    savingsGoals: Int,
    activeBudgets: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.MediumLarge),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickStatItem(
            value = transactionCount.toString(),
            label = "Transactions"
        )
        QuickStatItem(
            value = savingsGoals.toString(),
            label = "Goals"
        )
        QuickStatItem(
            value = activeBudgets.toString(),
            label = "Budgets"
        )
    }
}

@Composable
private fun QuickStatItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = ColorTokens.Primary500
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
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

        PyeraCard(
            modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge),
            borderColor = ColorBorder
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = ColorBorder,
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
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

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



