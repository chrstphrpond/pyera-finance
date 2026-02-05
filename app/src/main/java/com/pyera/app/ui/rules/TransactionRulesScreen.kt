package com.pyera.app.ui.rules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.pyera.app.R
import com.pyera.app.data.local.entity.MatchType
import com.pyera.app.data.local.entity.TransactionRuleEntity
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.ColorSuccess
import com.pyera.app.ui.theme.ColorWarning
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.SurfaceDark
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionRulesScreen(
    navController: NavController,
    viewModel: RulesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show error messages
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Show success messages
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rules_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.rules_back_content_desc),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.resetForm()
                    navController.navigate(Screen.AddTransactionRule.route)
                },
                containerColor = AccentGreen,
                contentColor = DeepBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.rules_add_rule_content_desc))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DeepBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Rule,
                        contentDescription = stringResource(R.string.rules_icon_content_desc),
                        tint = AccentGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.rules_info_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Rules list
            if (state.rules.isEmpty()) {
                EmptyRulesState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.rules,
                        key = { it.id }
                    ) { rule ->
                        RuleItem(
                            rule = rule,
                            onEdit = {
                                viewModel.loadRuleForEdit(rule.id)
                                navController.navigate(
                                    Screen.EditTransactionRule.createRoute(rule.id)
                                )
                            },
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteRule(rule.id)
                                }
                            },
                            onToggleActive = {
                                viewModel.toggleRuleActive(rule.id, !rule.isActive)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RuleItem(
    rule: TransactionRuleEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            val progress by animateFloatAsState(
                targetValue = if (dismissState.targetValue == DismissValue.DismissedToStart) 1f else 0f,
                label = "dismiss_progress"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorError.copy(alpha = progress * 0.8f))
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.rules_delete_button),
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        dismissContent = {
            RuleCard(
                rule = rule,
                onEdit = onEdit,
                onToggleActive = onToggleActive
            )
        }
    )
}

@Composable
private fun RuleCard(
    rule: TransactionRuleEntity,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    val matchTypeLabel = when (MatchType.fromString(rule.matchType)) {
        MatchType.CONTAINS -> stringResource(R.string.rules_match_type_contains)
        MatchType.STARTS_WITH -> stringResource(R.string.rules_match_type_starts_with)
        MatchType.ENDS_WITH -> stringResource(R.string.rules_match_type_ends_with)
        MatchType.EXACT -> stringResource(R.string.rules_match_type_exact)
        MatchType.REGEX -> stringResource(R.string.rules_match_type_regex)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isActive) SurfaceDark else SurfaceDark.copy(alpha = 0.5f)
        ),
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pattern badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = rule.pattern,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentGreen,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 200.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Priority badge
                    if (rule.priority > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        rule.priority >= 8 -> ColorError
                                        rule.priority >= 5 -> ColorWarning
                                        else -> ColorSuccess
                                    }.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "P${rule.priority}",
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    rule.priority >= 8 -> ColorError
                                    rule.priority >= 5 -> ColorWarning
                                    else -> ColorSuccess
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Active toggle
                    IconButton(onClick = onToggleActive) {
                        Icon(
                            imageVector = if (rule.isActive) 
                                Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            contentDescription = if (rule.isActive) stringResource(R.string.rules_active_content_desc) else stringResource(R.string.rules_inactive_content_desc),
                            tint = if (rule.isActive) AccentGreen else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Edit button
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.rules_edit_content_desc),
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Match type and category info
            Text(
                text = stringResource(R.string.rules_pattern_description, matchTypeLabel, rule.pattern),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Category indicator placeholder (category name would need to be fetched)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.rules_category_prefix, rule.categoryId),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }

            // Inactive indicator
            AnimatedVisibility(
                visible = !rule.isActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = stringResource(R.string.rules_inactive_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyRulesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Rule,
            contentDescription = stringResource(R.string.rules_no_rules_icon_content_desc),
            tint = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.rules_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.rules_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.rules_empty_hint),
            style = MaterialTheme.typography.bodySmall,
            color = AccentGreen
        )
    }
}
