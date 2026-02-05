package com.pyera.app.ui.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.AccountType
import com.pyera.app.data.local.entity.defaultIcon
import com.pyera.app.data.local.entity.displayName
import com.pyera.app.data.local.entity.formattedBalance
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    navController: NavController,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var showArchived by rememberSaveable { mutableStateOf(false) }
    
    // Filter accounts based on archived status
    val displayedAccounts = if (showArchived) {
        accounts
    } else {
        accounts.filter { !it.isArchived }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Toggle archived visibility
                    IconButton(onClick = { showArchived = !showArchived }) {
                        Icon(
                            if (showArchived) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showArchived) "Hide archived" else "Show archived"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBackground,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddAccount.route) },
                containerColor = AccentGreen,
                contentColor = DeepBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        },
        containerColor = DeepBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Total Balance Card
                TotalBalanceCard(totalBalance = totalBalance)
                
                // Accounts List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = displayedAccounts,
                        key = { it.id }
                    ) { account ->
                        AccountCard(
                            account = account,
                            onClick = {
                                navController.navigate(Screen.AccountDetail.createRoute(account.id))
                            },
                            onSetDefault = { viewModel.setDefaultAccount(account.id) },
                            onArchive = { viewModel.archiveAccount(account.id) },
                            onUnarchive = { viewModel.unarchiveAccount(account.id) }
                        )
                    }
                }
            }
            
            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            }
        }
    }
    
    // Error snackbar
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Could show snackbar here
            viewModel.clearError()
        }
    }
}

@Composable
private fun TotalBalanceCard(totalBalance: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₱${String.format("%,.2f", totalBalance)}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AccentGreen
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountCard(
    account: AccountEntity,
    onClick: () -> Unit,
    onSetDefault: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (account.isArchived) SurfaceDark else SurfaceElevated
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Account Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(account.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.icon,
                    fontSize = 28.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Account Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (account.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Default",
                            tint = AccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (account.isArchived) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Archived",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = account.type.displayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            // Balance
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = account.formattedBalance(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (account.balance >= 0) AccentGreen else ColorError,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = TextSecondary
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!account.isDefault && !account.isArchived) {
                        DropdownMenuItem(
                            text = { Text("Set as Default") },
                            onClick = {
                                onSetDefault()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Star, null)
                            }
                        )
                    }
                    
                    if (account.isArchived) {
                        DropdownMenuItem(
                            text = { Text("Unarchive") },
                            onClick = {
                                onUnarchive()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Unarchive, null)
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = {
                                onArchive()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Archive, null)
                            }
                        )
                    }
                }
            }
        }
    }
}
