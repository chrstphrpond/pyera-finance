package com.pyera.app.ui.dashboard

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.data.local.entity.TransactionTemplateEntity
import com.pyera.app.ui.templates.TemplatesViewModel
import com.pyera.app.ui.theme.DarkGreen
import com.pyera.app.ui.theme.NeonYellow
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary

/**
 * Horizontal row of quick template chips for the dashboard
 */
@Composable
fun QuickTemplatesRow(
    onTemplateClick: (Long) -> Unit,
    onSeeAllClick: () -> Unit,
    onAddTemplateClick: () -> Unit,
    viewModel: TemplatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadTemplates()
    }

    // Filter to show only active templates, sorted by usage
    val templates = uiState.templates
        .filter { it.isActive }
        .sortedByDescending { it.useCount }
        .take(5)

    if (templates.isEmpty()) {
        // Show empty state with add button
        EmptyTemplatesRow(onAddTemplateClick)
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Templates",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonYellow,
                    modifier = Modifier.clickable(onClick = onSeeAllClick)
                )
            }

            // Templates row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(templates, key = { it.id }) { template ->
                    TemplateChip(
                        template = template,
                        onClick = { onTemplateClick(template.id) }
                    )
                }

                // Add button at the end
                item {
                    AddTemplateChip(onClick = onAddTemplateClick)
                }
            }
        }
    }
}

@Composable
private fun TemplateChip(
    template: TransactionTemplateEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceElevated
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NeonYellow.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = template.getDisplayIcon(),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Name
            Text(
                text = template.name,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AddTemplateChip(onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceElevated.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = TextSecondary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(TextSecondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Add",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyTemplatesRow(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onAddClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceElevated.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = TextSecondary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = NeonYellow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create templates for quick transactions",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
