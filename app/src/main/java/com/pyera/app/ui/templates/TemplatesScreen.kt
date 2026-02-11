package com.pyera.app.ui.templates

import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.data.local.entity.TransactionTemplateEntity
import com.pyera.app.ui.components.EmptyState
import com.pyera.app.ui.util.pyeraBackground
import androidx.compose.ui.res.stringResource
import com.pyera.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onNavigateBack: () -> Unit,
    onAddTemplate: () -> Unit,
    onEditTemplate: (TransactionTemplateEntity) -> Unit,
    onUseTemplate: ((Long) -> Unit)? = null,
    viewModel: TemplatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Store only the ID (which is saveable) instead of the entire entity
    var templateToDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val templateToDelete = templateToDeleteId?.let { id ->
        uiState.templates.find { it.id == id }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.templates_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.templates_navigate_back_content_desc),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTemplate,
                containerColor = ColorTokens.Primary500,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.templates_add_content_desc))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(paddingValues)
                .padding(horizontal = SpacingTokens.Medium)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::searchTemplates,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.templates_search_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.templates_search_content_desc), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorTokens.SurfaceLevel2,
                    unfocusedContainerColor = ColorTokens.SurfaceLevel2,
                    focusedBorderColor = ColorTokens.Primary500,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Templates grid
            val filteredTemplates = uiState.templates.filter {
                it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                it.description.contains(uiState.searchQuery, ignoreCase = true)
            }

            if (filteredTemplates.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    icon = if (uiState.searchQuery.isEmpty()) Icons.Default.Edit else Icons.Default.Search,
                    title = if (uiState.searchQuery.isEmpty()) stringResource(R.string.templates_empty_title) else stringResource(R.string.templates_no_results),
                    subtitle = if (uiState.searchQuery.isEmpty()) {
                        stringResource(R.string.templates_empty_message)
                    } else {
                        stringResource(R.string.templates_no_results_message)
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTemplates, key = { it.id }) { template ->
                        TemplateCard(
                            template = template,
                            onClick = {
                                onUseTemplate?.invoke(template.id)
                            },
                            onEdit = { onEditTemplate(template) },
                            onDelete = { templateToDeleteId = template.id },
                            onToggleActive = { isActive ->
                                viewModel.toggleTemplateActive(template.id, isActive)
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (templateToDelete != null) {
        AlertDialog(
            onDismissRequest = { templateToDeleteId = null },
            title = { Text(stringResource(R.string.templates_delete_dialog_title), color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Text(
                    "Are you sure you want to delete \"${templateToDelete!!.name}\"? This action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTemplate(templateToDelete!!.id)
                        templateToDeleteId = null
                    }
                ) {
                    Text(stringResource(R.string.templates_delete_button), color = ColorTokens.Error500)
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDeleteId = null }) {
                    Text(stringResource(R.string.templates_cancel_button), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = ColorTokens.SurfaceLevel2
        )
    }
}

@Composable
private fun TemplateCard(
    template: TransactionTemplateEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }

    PyeraCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(enabled = template.isActive, onClick = onClick),
        cornerRadius = SpacingTokens.Medium,
        containerColor = if (template.isActive) ColorTokens.SurfaceLevel2 else ColorTokens.SurfaceLevel2.copy(alpha = 0.5f),
        borderWidth = 0.dp,
        elevation = if (template.isActive) 4.dp else 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon
                Text(
                    text = template.getDisplayIcon(),
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Name
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (template.isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount
                Text(
                    text = template.getAmountDisplay(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (template.type == "INCOME") ColorTokens.Primary500 else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Type badge
                Text(
                    text = template.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Menu button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(SpacingTokens.ExtraLarge)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.templates_more_options_content_desc),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = ColorTokens.SurfaceLevel2
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.templates_edit_menu), color = MaterialTheme.colorScheme.onBackground) },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.templates_edit_menu), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (template.isActive) stringResource(R.string.templates_deactivate_menu) else stringResource(R.string.templates_activate_menu),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        leadingIcon = {
                            Switch(
                                checked = template.isActive,
                                onCheckedChange = null,
                                modifier = Modifier.size(SpacingTokens.ExtraLarge)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleActive(!template.isActive)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.templates_delete_button), color = ColorTokens.Error500) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.templates_delete_button), tint = ColorTokens.Error500)
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }

            // Inactive indicator
            if (!template.isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.templates_inactive_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}




