package com.pyera.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import com.pyera.app.ui.transaction.TransactionFilter
import com.pyera.app.ui.transaction.TransactionSort
import com.pyera.app.ui.transaction.DateRangeFilter

/**
 * A row of filter chips for transaction type filtering.
 * Includes: All, Income, Expense
 *
 * @param selectedFilter Currently selected filter
 * @param onFilterSelected Callback when a filter is selected
 * @param modifier Modifier for customizing the layout
 */
@Composable
fun TransactionFilterChips(
    selectedFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All Filter
        FilterChip(
            selected = selectedFilter == TransactionFilter.ALL,
            onClick = { onFilterSelected(TransactionFilter.ALL) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ColorTokens.Primary500,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = ColorTokens.SurfaceLevel2,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Income Filter
        FilterChip(
            selected = selectedFilter == TransactionFilter.INCOME,
            onClick = { onFilterSelected(TransactionFilter.INCOME) },
            label = { Text("Income") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ColorTokens.Success500.copy(alpha = 0.3f),
                selectedLabelColor = ColorTokens.Success500,
                containerColor = ColorTokens.SurfaceLevel2,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Expense Filter
        FilterChip(
            selected = selectedFilter == TransactionFilter.EXPENSE,
            onClick = { onFilterSelected(TransactionFilter.EXPENSE) },
            label = { Text("Expense") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ColorTokens.Error500.copy(alpha = 0.3f),
                selectedLabelColor = ColorTokens.Error500,
                containerColor = ColorTokens.SurfaceLevel2,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * Category filter chip with dropdown menu.
 *
 * @param categories List of available categories
 * @param selectedCategoryId Currently selected category ID (null for all)
 * @param onCategorySelected Callback when a category is selected (null for all)
 * @param modifier Modifier for customizing the layout
 */
@Composable
fun CategoryFilterChip(
    categories: List<CategoryEntity>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    Box(modifier = modifier) {
        FilterChip(
            selected = selectedCategoryId != null,
            onClick = { showDropdown = true },
            label = { 
                Text(selectedCategory?.name ?: "Category") 
            },
            leadingIcon = if (selectedCategoryId != null) {
                {
                    Box(
                        modifier = Modifier
                            .size(SpacingTokens.Medium)
                            .clip(CircleShape)
                            .background(Color(selectedCategory?.color ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f).hashCode()))
                    )
                }
            } else null,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select category",
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ColorTokens.Primary500.copy(alpha = 0.2f),
                selectedLabelColor = ColorTokens.Primary500,
                containerColor = ColorTokens.SurfaceLevel2,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.background(ColorTokens.SurfaceLevel2)
        ) {
            DropdownMenuItem(
                text = { Text("All Categories", color = MaterialTheme.colorScheme.onBackground) },
                onClick = {
                    onCategorySelected(null)
                    showDropdown = false
                }
            )

            if (categories.isNotEmpty()) {
                HorizontalDivider(color = ColorBorder)
            }

            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name, color = MaterialTheme.colorScheme.onBackground) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(SpacingTokens.Medium)
                                .clip(CircleShape)
                                .background(Color(category.color))
                        )
                    },
                    onClick = {
                        onCategorySelected(category.id)
                        showDropdown = false
                    }
                )
            }
        }
    }
}

/**
 * Date range filter chips.
 * Includes: Today, This Week, This Month
 *
 * @param selectedDateRange Currently selected date range
 * @param onDateRangeSelected Callback when a date range is selected
 * @param modifier Modifier for customizing the layout
 */
@Composable
fun DateRangeFilterChips(
    selectedDateRange: DateRangeFilter,
    onDateRangeSelected: (DateRangeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Today
        DateFilterChip(
            label = "Today",
            selected = selectedDateRange == DateRangeFilter.TODAY,
            onClick = { 
                onDateRangeSelected(
                    if (selectedDateRange == DateRangeFilter.TODAY) 
                        DateRangeFilter.ALL 
                    else 
                        DateRangeFilter.TODAY
                )
            }
        )

        // This Week
        DateFilterChip(
            label = "This Week",
            selected = selectedDateRange == DateRangeFilter.THIS_WEEK,
            onClick = { 
                onDateRangeSelected(
                    if (selectedDateRange == DateRangeFilter.THIS_WEEK) 
                        DateRangeFilter.ALL 
                    else 
                        DateRangeFilter.THIS_WEEK
                )
            }
        )

        // This Month
        DateFilterChip(
            label = "This Month",
            selected = selectedDateRange == DateRangeFilter.THIS_MONTH,
            onClick = { 
                onDateRangeSelected(
                    if (selectedDateRange == DateRangeFilter.THIS_MONTH) 
                        DateRangeFilter.ALL 
                    else 
                        DateRangeFilter.THIS_MONTH
                )
            }
        )
    }
}

/**
 * Individual date filter chip with consistent styling.
 */
@Composable
private fun DateFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Default.DateRange, 
                    null, 
                    modifier = Modifier.size(SpacingTokens.Medium)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ColorTokens.Primary500.copy(alpha = 0.2f),
            selectedLabelColor = ColorTokens.Primary500,
            containerColor = ColorTokens.SurfaceLevel2,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * Sort dropdown menu for transaction list.
 *
 * @param selectedSort Currently selected sort option
 * @param onSortSelected Callback when a sort option is selected
 * @param modifier Modifier for customizing the layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDropdown(
    selectedSort: TransactionSort,
    onSortSelected: (TransactionSort) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val sortLabel = when (selectedSort) {
        TransactionSort.DATE_DESC -> "Newest First"
        TransactionSort.DATE_ASC -> "Oldest First"
        TransactionSort.AMOUNT_DESC -> "Highest Amount"
        TransactionSort.AMOUNT_ASC -> "Lowest Amount"
        TransactionSort.CATEGORY_ASC -> "Category"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        InputChip(
            selected = false,
            onClick = { expanded = true },
            label = { Text(sortLabel) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort",
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select sort order",
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = InputChipDefaults.inputChipColors(
                containerColor = ColorTokens.SurfaceLevel2,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ColorTokens.SurfaceLevel2)
        ) {
            DropdownMenuItem(
                text = { Text("Newest First", color = MaterialTheme.colorScheme.onBackground) },
                onClick = {
                    onSortSelected(TransactionSort.DATE_DESC)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Oldest First", color = MaterialTheme.colorScheme.onBackground) },
                onClick = {
                    onSortSelected(TransactionSort.DATE_ASC)
                    expanded = false
                }
            )
            HorizontalDivider(color = ColorBorder)
            DropdownMenuItem(
                text = { Text("Highest Amount", color = MaterialTheme.colorScheme.onBackground) },
                onClick = {
                    onSortSelected(TransactionSort.AMOUNT_DESC)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Lowest Amount", color = MaterialTheme.colorScheme.onBackground) },
                onClick = {
                    onSortSelected(TransactionSort.AMOUNT_ASC)
                    expanded = false
                }
            )
            HorizontalDivider(color = ColorBorder)
            DropdownMenuItem(
                text = { Text("Category", color = MaterialTheme.colorScheme.onBackground) },
                onClick = {
                    onSortSelected(TransactionSort.CATEGORY_ASC)
                    expanded = false
                }
            )
        }
    }
}

/**
 * Clear filters button chip.
 * Only visible when filters are applied.
 *
 * @param visible Whether the clear button should be visible
 * @param onClear Callback when clear is clicked
 * @param modifier Modifier for customizing the layout
 */
@Composable
fun ClearFiltersChip(
    visible: Boolean,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        AssistChip(
            onClick = onClear,
            label = { Text("Clear") },
            leadingIcon = {
                Icon(
                    Icons.Default.Clear, 
                    null, 
                    modifier = Modifier.size(SpacingTokens.Medium),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = ColorTokens.SurfaceLevel2.copy(alpha = 0.5f)
            ),
            modifier = modifier
        )
    }
}


