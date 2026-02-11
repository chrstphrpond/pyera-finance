package com.pyera.app.ui.settings

import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.data.preferences.ThemeMode
import com.pyera.app.ui.components.PyeraButton
import com.pyera.app.ui.theme.PyeraTheme
import com.pyera.app.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance", color = colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface
                )
            )
        },
        containerColor = colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Theme Preview Card
            ThemePreviewCard(themeMode = themeMode)
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            
            // Theme Mode Selection
            Text(
                text = "Theme Mode",
                style = MaterialTheme.typography.labelLarge,
                color = ColorTokens.Primary500,
                modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge, vertical = SpacingTokens.Small)
            )
            
            PyeraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.MediumLarge),
                cornerRadius = 12.dp,
                containerColor = ColorTokens.SurfaceLevel2,
                borderWidth = 0.dp
            ) {
                Column {
                    ThemeModeOption(
                        icon = Icons.Default.SettingsSuggest,
                        title = "System Default",
                        subtitle = "Follows your device settings",
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                    )
                    
                    HorizontalDivider(
                        color = colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge)
                    )
                    
                    ThemeModeOption(
                        icon = Icons.Default.LightMode,
                        title = "Light",
                        subtitle = "Always use light theme",
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                    )
                    
                    HorizontalDivider(
                        color = colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge)
                    )
                    
                    ThemeModeOption(
                        icon = Icons.Default.DarkMode,
                        title = "Dark",
                        subtitle = "Always use dark theme",
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            
            // Apply Button
            PyeraButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.MediumLarge)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Apply")
            }
            
            Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))
        }
    }
}

@Composable
private fun ThemePreviewCard(themeMode: ThemeMode) {
    PyeraCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.MediumLarge)
            .height(180.dp),
        cornerRadius = SpacingTokens.Medium,
        containerColor = ColorTokens.SurfaceLevel2,
        borderWidth = 0.dp
    ) {
        PyeraTheme(themeMode = themeMode) {
            val colorScheme = MaterialTheme.colorScheme
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Preview Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SpacingTokens.Medium)
                ) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Sample Card Preview
                    PyeraCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 12.dp,
                        containerColor = colorScheme.surface,
                        borderWidth = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(SpacingTokens.Medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Balance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$12,450.00",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary
                                )
                            }
                            
                            // Mini chart indicator
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        colorScheme.primary.copy(alpha = 0.12f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(SpacingTokens.Large)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Theme indicator
                    Text(
                        text = when (themeMode) {
                            ThemeMode.SYSTEM -> "Following system settings"
                            ThemeMode.LIGHT -> "Light theme active"
                            ThemeMode.DARK -> "Dark theme active"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

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
                tint = if (selected) ColorTokens.Primary500 else colorScheme.onSurfaceVariant
            )
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = ColorTokens.Primary500,
                unselectedColor = colorScheme.onSurfaceVariant
            )
        )
    }
}





