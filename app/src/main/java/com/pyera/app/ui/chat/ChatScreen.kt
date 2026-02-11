package com.pyera.app.ui.chat

import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.util.pyeraBackground

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var prompt by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pyera AI Assistant", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(paddingValues)
                .padding(SpacingTokens.Medium)
        ) {
            // Info card indicating chat is disabled
            PyeraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SpacingTokens.Medium),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                borderWidth = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(SpacingTokens.Medium)
                ) {
                    Text(
                        text = "ℹ️ AI Chat Disabled",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The AI assistant has been disabled for security reasons. You can still use all other Pyera features to manage your finances.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)
            ) {
                if (state.messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(SpacingTokens.ExtraLarge),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ask me anything about your finances!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                items(state.messages) { message ->
                    MessageBubble(message = message)
                }

                if (state.isLoading) {
                    item {
                         Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(SpacingTokens.Large),
                                color = ColorTokens.Primary500
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Type a message...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = ColorTokens.Primary500,
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(SpacingTokens.Large),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        viewModel.sendMessage(prompt)
                        prompt = ""
                    },
                    containerColor = ColorTokens.Primary500,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isUser) ColorTokens.Primary500 else CardBackground
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
    val shape = if (message.isUser) {
        RoundedCornerShape(topStart = SpacingTokens.Medium, topEnd = 4.dp, bottomStart = SpacingTokens.Medium, bottomEnd = SpacingTokens.Medium)
    } else {
         RoundedCornerShape(topStart = 4.dp, topEnd = SpacingTokens.Medium, bottomStart = SpacingTokens.Medium, bottomEnd = SpacingTokens.Medium)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(bgColor, shape)
                    .padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}




