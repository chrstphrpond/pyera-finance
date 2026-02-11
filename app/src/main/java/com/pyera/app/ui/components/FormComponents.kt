package com.pyera.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pyera.app.ui.theme.ColorBorder
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

/**
 * A standardized text field component with consistent styling.
 * 
 * @param value The current text value
 * @param onValueChange Callback when text changes
 * @param label Optional label text
 * @param placeholder Optional placeholder text
 * @param modifier Modifier for layout
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display
 * @param keyboardType Keyboard type for input
 * @param imeAction IME action for keyboard
 * @param onImeAction Callback when IME action is triggered
 * @param singleLine Whether the field is single line
 * @param maxLines Maximum number of lines
 */
@Composable
fun PyeraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)) } },
        leadingIcon = leadingIcon?.let {
            { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        trailingIcon = trailingIcon?.let {
            { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = ColorTokens.Error500) }
        } else null,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction?.invoke() },
            onSearch = { onImeAction?.invoke() },
            onSend = { onImeAction?.invoke() },
            onGo = { onImeAction?.invoke() },
            onNext = { onImeAction?.invoke() },
            onPrevious = { onImeAction?.invoke() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ColorTokens.Primary500,
            unfocusedBorderColor = ColorBorder,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedLabelColor = ColorTokens.Primary500,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            errorBorderColor = ColorTokens.Error500,
            errorLabelColor = ColorTokens.Error500,
            cursorColor = ColorTokens.Primary500
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * A standardized dropdown component with consistent styling.
 * 
 * @param label The label for the dropdown
 * @param selectedItem The currently selected item (null if none)
 * @param items List of items to display
 * @param itemLabel Function to get display text for each item
 * @param onItemSelected Callback when an item is selected
 * @param modifier Modifier for layout
 */
@Composable
fun <T> PyeraDropdown(
    label: String,
    selectedItem: T?,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedItem?.let(itemLabel) ?: "",
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Open dropdown",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorTokens.Primary500,
                unfocusedBorderColor = ColorBorder,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = ColorTokens.Primary500,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        
        // Invisible overlay to handle clicks
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(ColorTokens.SurfaceLevel2)
                .width(280.dp)
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = itemLabel(item),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    trailingIcon = if (item == selectedItem) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = ColorTokens.Primary500
                            )
                        }
                    } else null,
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
                
                if (index < items.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.Medium)
                    )
                }
            }
        }
    }
}

/**
 * A searchable dropdown component with text input filtering.
 * 
 * @param label The label for the dropdown
 * @param query Current search query
 * @param onQueryChange Callback when search query changes
 * @param selectedItem The currently selected item
 * @param items Filtered list of items to display
 * @param itemLabel Function to get display text for each item
 * @param onItemSelected Callback when an item is selected
 * @param modifier Modifier for layout
 * @param placeholder Placeholder text when no selection
 */
@Composable
fun <T> PyeraSearchableDropdown(
    label: String,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedItem: T?,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Column {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    onQueryChange(it)
                    expanded = true
                },
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Open dropdown",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorTokens.Primary500,
                    unfocusedBorderColor = ColorBorder,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedLabelColor = ColorTokens.Primary500,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            DropdownMenu(
                expanded = expanded && items.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(ColorTokens.SurfaceLevel2)
                    .width(280.dp)
            ) {
                items.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = itemLabel(item),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        trailingIcon = if (item == selectedItem) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = ColorTokens.Primary500
                                )
                            }
                        } else null,
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                    
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = SpacingTokens.Medium)
                        )
                    }
                }
            }
        }
    }
}



