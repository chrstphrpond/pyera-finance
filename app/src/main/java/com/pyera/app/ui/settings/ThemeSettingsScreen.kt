package com.pyera.app.ui.settings

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.pyera.app.ui.theme.DarkGreen
import com.pyera.app.ui.theme.NeonYellow
import com.pyera.app.ui.theme.Spacing
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary
import com.pyera.app.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance", color = TextPrimary) },
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
            // Theme Preview Card
            ThemePreviewCard(themeMode = themeMode)
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            // Theme Mode Selection
            Text(
                text = "Theme Mode",
                style = MaterialTheme.typography.labelLarge,
                color = NeonYellow,
                modifier = Modifier.padding(horizontal = Spacing.ScreenPadding, vertical = Spacing.Small)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.ScreenPadding),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp)
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
                        color = DarkGreen,
                        modifier = Modifier.padding(horizontal = Spacing.CardPadding)
                    )
                    
                    ThemeModeOption(
                        icon = Icons.Default.LightMode,
                        title = "Light",
                        subtitle = "Always use light theme",
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                    )
                    
                    HorizontalDivider(
                        color = DarkGreen,
                        modifier = Modifier.padding(horizontal = Spacing.CardPadding)
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
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            // Apply Button
            PyeraButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.ScreenPadding)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Apply")
            }
            
            Spacer(modifier = Modifier.height(Spacing.XXXLarge))
        }
    }
}

@Composable
private fun ThemePreviewCard(themeMode: ThemeMode) {
    val isDarkPreview = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> true // Default to dark for preview
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.ScreenPadding)
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceElevated
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Preview Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sample Card Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkPreview) 
                            com.pyera.app.ui.theme.SurfaceDark 
                        else 
                            androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Balance",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkPreview) TextSecondary else androidx.compose.ui.graphics.Color.Gray
                            )
                            Text(
                                text = "$12,450.00",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkPreview) NeonYellow else androidx.compose.ui.graphics.Color(0xFF2E7D32)
                            )
                        }
                        
                        // Mini chart indicator
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isDarkPreview) NeonYellow.copy(alpha = 0.2f)
                                    else androidx.compose.ui.graphics.Color(0xFF2E7D32).copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isDarkPreview) NeonYellow else androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
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
                    color = TextSecondary
                )
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
                tint = if (selected) NeonYellow else TextSecondary
            )
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = NeonYellow,
                unselectedColor = TextSecondary
            )
        )
    }
}
