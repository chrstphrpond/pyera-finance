# Pyera Finance - Complete Screen Redesign Plan

**Version:** 1.0  
**Date:** February 2026  
**Scope:** All 22 Screens  
**Design System:** Midnight Slate + Trust Blue

---

## Table of Contents

1. [Screen Inventory](#screen-inventory)
2. [Design Principles](#design-principles)
3. [Screen-by-Screen Specifications](#screen-by-screen-specifications)
4. [Component Mapping](#component-mapping)
5. [Implementation Priority](#implementation-priority)
6. [Migration Strategy](#migration-strategy)

---

## Screen Inventory

### Authentication (2 screens)
| # | Screen | File | Priority | Complexity |
|---|--------|------|----------|------------|
| 1 | Login | `LoginScreen.kt` | High | Medium |
| 2 | Register | `RegisterScreen.kt` | High | Medium |

### Main App - Core (6 screens)
| # | Screen | File | Priority | Complexity |
|---|--------|------|----------|------------|
| 3 | Dashboard | `DashboardScreen.kt` | Critical | High |
| 4 | Transaction List | `TransactionListScreen.kt` | Critical | High |
| 5 | Add Transaction | `AddTransactionScreen.kt` | Critical | Medium |
| 6 | Budget List | `BudgetScreen.kt` | High | Medium |
| 7 | Budget Detail | `BudgetDetailScreen.kt` | Medium | Low |
| 8 | Accounts | `AccountsScreen.kt` | High | Medium |

### Financial Management (6 screens)
| # | Screen | File | Priority | Complexity |
|---|--------|------|----------|------------|
| 9 | Account Detail | `AccountDetailScreen.kt` | Medium | Low |
| 10 | Transfer | `TransferScreen.kt` | High | Medium |
| 11 | Debt | `DebtScreen.kt` | Medium | Medium |
| 12 | Savings | `SavingsScreen.kt` | Medium | Medium |
| 13 | Investments | `InvestmentsScreen.kt` | Low | Medium |
| 14 | Bills | `BillsScreen.kt` | Low | Low |

### Analysis & Insights (3 screens)
| # | Screen | File | Priority | Complexity |
|---|--------|------|----------|------------|
| 15 | Analysis | `AnalysisScreen.kt` | Medium | High |
| 16 | Insights | `InsightsScreen.kt` | Medium | High |
| 17 | Chat | `ChatScreen.kt` | Low | Medium |

### Settings & Profile (5 screens)
| # | Screen | File | Priority | Complexity |
|---|--------|------|----------|------------|
| 18 | Profile | `ProfileScreen.kt` | Medium | Low |
| 19 | Theme Settings | `ThemeSettingsScreen.kt` | Low | Low |
| 20 | Security Settings | `SecuritySettingsScreen.kt` | Medium | Medium |
| 21 | App Lock | `AppLockScreen.kt` | High | Low |
| 22 | Set PIN | `SetPinScreen.kt` | High | Low |

---

## Design Principles

### 1. Visual Hierarchy
- **Primary actions:** Trust Blue (`Primary500`)
- **Success/Positive:** Growth Green (`Success500`)
- **Error/Negative:** Alert Red (`Error500`)
- **Backgrounds:** Midnight Slate gradient (`SurfaceLevel0` → `SurfaceLevel1`)

### 2. Spacing Rules
- **Screen padding:** 16dp (`SpacingTokens.Medium`)
- **Between cards:** 12dp (`SpacingTokens.MediumSmall`)
- **Within cards:** 16dp (`SpacingTokens.Medium`)
- **Section breaks:** 24dp (`SpacingTokens.Large`)

### 3. Typography Scale
- **Hero amounts:** `MoneyLarge` (40sp)
- **Card titles:** `titleLarge` (22sp)
- **Body text:** `bodyLarge` (16sp)
- **Captions:** `bodySmall` (12sp)

### 4. Component Usage
- **Cards:** Always use `PyeraCard()` with appropriate variant
- **Money:** Always use `MoneyDisplay()` with animation
- **Buttons:** Material 3 `Button()`, `OutlinedButton()`, `TextButton()`
- **Icons:** Phosphor Icons (24dp default)

---

## Screen-by-Screen Specifications

### 1. Login Screen

**Current Issues:**
- Mixed color aliases
- Inconsistent spacing
- No modern Material 3 patterns

**Redesign Specifications:**

```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorTokens.SurfaceLevel0)
    ) {
        // Subtle gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ColorTokens.Primary900.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpacingTokens.Large)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "Pyera",
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.XXL))
            
            // Title
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.XXL))
            
            // Form Card
            PyeraCard(
                variant = CardVariant.Elevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(SpacingTokens.Large)
                ) {
                    // Email
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(RadiusTokens.Medium)
                    )
                    
                    Spacer(modifier = Modifier.height(SpacingTokens.Medium))
                    
                    // Password
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = { /* Eye icon */ },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(RadiusTokens.Medium)
                    )
                    
                    Spacer(modifier = Modifier.height(SpacingTokens.Small))
                    
                    // Forgot password
                    TextButton(
                        onClick = { /* Navigate */ },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Forgot Password?")
                    }
                    
                    Spacer(modifier = Modifier.height(SpacingTokens.Medium))
                    
                    // Sign In Button
                    Button(
                        onClick = { viewModel.login() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.isFormValid && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign In")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(SpacingTokens.Large))
            
            // Google Sign In
            OutlinedButton(
                onClick = { viewModel.signInWithGoogle() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(SpacingTokens.Small))
                Text("Continue with Google")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Sign up link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { /* Navigate */ }) {
                    Text("Sign Up")
                }
            }
        }
    }
}
```

**Key Changes:**
- ✅ Use `ColorTokens.SurfaceLevel0` for background
- ✅ Add radial gradient with `Primary900`
- ✅ Use `PyeraCard(variant = CardVariant.Elevated)` for form
- ✅ Update spacing to use `SpacingTokens`
- ✅ Material 3 `Button` with loading state

---

### 2. Dashboard Screen

**Current Issues:**
- Cluttered layout
- Inconsistent card styles
- No animation on balance

**Redesign Specifications:**

```kotlin
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    Scaffold(
        bottomBar = { PyeraBottomNavigation(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
        ) {
            // HERO BALANCE CARD
            PyeraCard(
                variant = CardVariant.Elevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.Medium)
            ) {
                Column(
                    modifier = Modifier.padding(SpacingTokens.Large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(SpacingTokens.Small))
                    
                    // Animated balance
                    MoneyDisplay(
                        amount = uiState.totalBalance,
                        size = MoneySize.Large,
                        animate = true
                    )
                    
                    Spacer(modifier = Modifier.height(SpacingTokens.Large))
                    
                    // Income / Expense Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricItem(
                            icon = PhosphorIcons.Regular.ArrowUp,
                            label = "Income",
                            amount = uiState.monthlyIncome,
                            color = ColorTokens.Success500
                        )
                        
                        VerticalDivider(
                            modifier = Modifier.height(40.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        
                        MetricItem(
                            icon = PhosphorIcons.Regular.ArrowDown,
                            label = "Expense",
                            amount = uiState.monthlyExpense,
                            color = ColorTokens.Error500
                        )
                    }
                }
            }
            
            // QUICK ACTIONS
            QuickActionsRow(
                actions = listOf(
                    QuickAction("Add", PhosphorIcons.Regular.Plus) { /* Navigate */ },
                    QuickAction("Scan", PhosphorIcons.Regular.Scanner) { /* Navigate */ },
                    QuickAction("Transfer", PhosphorIcons.Regular.ArrowRight) { /* Navigate */ },
                    QuickAction("Budget", PhosphorIcons.Regular.ChartPieSlice) { /* Navigate */ }
                ),
                modifier = Modifier.padding(vertical = SpacingTokens.Medium)
            )
            
            // RECENT TRANSACTIONS
            SectionHeader(
                title = "Recent Transactions",
                action = "See All",
                onActionClick = { /* Navigate */ },
                modifier = Modifier.padding(horizontal = SpacingTokens.Medium)
            )
            
            Column(
                modifier = Modifier.padding(horizontal = SpacingTokens.Medium)
            ) {
                uiState.recentTransactions.take(5).forEach { transaction ->
                    TransactionListItem(
                        transaction = transaction,
                        onClick = { /* Detail */ },
                        onEdit = { /* Edit */ },
                        onDelete = { /* Delete */ }
                    )
                    Spacer(modifier = Modifier.height(SpacingTokens.Small))
                }
            }
            
            // ACTIVE BUDGETS
            if (uiState.activeBudgets.isNotEmpty()) {
                SectionHeader(
                    title = "Active Budgets",
                    modifier = Modifier.padding(SpacingTokens.Medium)
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = SpacingTokens.Medium),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)
                ) {
                    items(uiState.activeBudgets) { budget ->
                        BudgetPreviewCard(budget = budget)
                    }
                }
            }
            
            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    label: String,
    amount: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.ExtraSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(SpacingTokens.ExtraSmall))
        
        MoneyDisplay(
            amount = amount,
            size = MoneySize.Small,
            isPositive = color == ColorTokens.Success500
        )
    }
}

@Composable
private fun QuickActionsRow(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.Medium),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        actions.forEach { action ->
            QuickActionButton(action = action)
        }
    }
}

@Composable
private fun QuickActionButton(action: QuickAction) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = action.onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = ColorTokens.Primary500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(RadiusTokens.Large)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = ColorTokens.Primary500,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(SpacingTokens.Small))
        
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.Small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        
        if (action != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(action)
            }
        }
    }
}
```

**Key Changes:**
- ✅ Hero card with `CardVariant.Elevated`
- ✅ Animated `MoneyDisplay` for balance
- ✅ Horizontal quick actions with icons
- ✅ `TransactionListItem` with swipe actions
- ✅ Horizontal budget carousel
- ✅ Consistent `SpacingTokens` usage

---

### 3. Transaction List Screen

**Redesign Specifications:**

```kotlin
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    IconButton(onClick = { /* Show search */ }) {
                        Icon(PhosphorIcons.Regular.MagnifyingGlass, "Search")
                    }
                    IconButton(onClick = { /* Show filter bottom sheet */ }) {
                        Icon(PhosphorIcons.Regular.Funnel, "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Navigate to add */ },
                icon = { Icon(PhosphorIcons.Regular.Plus, null) },
                text = { Text("Add") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.groupedTransactions.isEmpty()) {
                EmptyTransactionsState()
            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() }
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(SpacingTokens.Medium)
                    ) {
                        uiState.groupedTransactions.forEach { (date, transactions) ->
                            // Sticky date header
                            stickyHeader(key = date) {
                                DateHeader(date = date)
                            }
                            
                            items(
                                items = transactions,
                                key = { it.id }
                            ) { transaction ->
                                TransactionListItem(
                                    transaction = transaction,
                                    onClick = { /* Detail */ },
                                    onEdit = { /* Edit */ },
                                    onDelete = { /* Delete */ }
                                )
                                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeader(date: String) {
    Surface(
        color = ColorTokens.SurfaceLevel0.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = SpacingTokens.Medium,
                vertical = SpacingTokens.Small
            )
        )
    }
}

@Composable
private fun EmptyTransactionsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.XXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.Receipt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = ColorTokens.Slate600
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Large))
        
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = "Start tracking your expenses by adding your first transaction",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = SpacingTokens.Small)
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Large))
        
        Button(onClick = { /* Navigate to add */ }) {
            Text("Add Transaction")
        }
    }
}
```

---

### 4. Budget Screen

**Redesign Specifications:**

```kotlin
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Budgets") },
                actions = {
                    IconButton(onClick = { /* Info */ }) {
                        Icon(PhosphorIcons.Regular.Info, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Create budget */ }) {
                Icon(PhosphorIcons.Regular.Plus, "Add Budget")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(SpacingTokens.Medium),
            modifier = Modifier.padding(padding)
        ) {
            // Summary card
            item {
                BudgetSummaryCard(
                    totalBudget = uiState.totalBudget,
                    totalSpent = uiState.totalSpent,
                    overBudgetCount = uiState.overBudgetCount
                )
                Spacer(modifier = Modifier.height(SpacingTokens.Large))
            }
            
            // Budget list
            items(uiState.budgets, key = { it.id }) { budget ->
                BudgetCard(
                    budget = budget,
                    onClick = { /* Navigate to detail */ }
                )
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            }
        }
    }
}

@Composable
private fun BudgetSummaryCard(
    totalBudget: Double,
    totalSpent: Double,
    overBudgetCount: Int
) {
    val totalProgress = (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f)
    
    PyeraCard(variant = CardVariant.Elevated) {
        Column(
            modifier = Modifier.padding(SpacingTokens.Large)
        ) {
            Text(
                text = "Monthly Overview",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyDisplay(
                        amount = totalBudget,
                        size = MoneySize.Medium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyDisplay(
                        amount = totalSpent,
                        size = MoneySize.Medium,
                        isPositive = totalSpent <= totalBudget
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            
            // Overall progress
            LinearProgressIndicator(
                progress = { totalProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Full)),
                color = when {
                    totalProgress > 1f -> ColorTokens.Error500
                    totalProgress > 0.8f -> ColorTokens.Warning500
                    else -> ColorTokens.Success500
                },
                trackColor = ColorTokens.Slate800
            )
            
            if (overBudgetCount > 0) {
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                Text(
                    text = "$overBudgetCount budget${if (overBudgetCount > 1) "s" else ""} over limit",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorTokens.Error500
                )
            }
        }
    }
}

@Composable
private fun BudgetCard(
    budget: BudgetWithSpending,
    onClick: () -> Unit
) {
    val progress = (budget.spentAmount / budget.amount).toFloat().coerceIn(0f, 1f)
    val isOverBudget = budget.spentAmount > budget.amount
    val isNearLimit = progress > 0.8f && !isOverBudget
    
    val progressColor = when {
        isOverBudget -> ColorTokens.Error500
        isNearLimit -> ColorTokens.Warning500
        else -> ColorTokens.Success500
    }
    
    PyeraCard(onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = progressColor.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(budget.categoryName),
                            contentDescription = null,
                            tint = progressColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(SpacingTokens.Small))
                    
                    Text(
                        text = budget.categoryName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                // Remaining amount
                MoneyDisplay(
                    amount = budget.remainingAmount,
                    size = MoneySize.Small,
                    isPositive = !isOverBudget
                )
            }
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Full)),
                color = progressColor,
                trackColor = ColorTokens.Slate800
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.Small))
            
            // Footer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val percentage = (progress * 100).toInt()
                Text(
                    text = "$percentage% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = progressColor
                )
                
                Text(
                    text = "${formatCurrency(budget.spentAmount)} of ${formatCurrency(budget.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isOverBudget) {
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                Text(
                    text = "⚠ Over budget by ${formatCurrency(budget.spentAmount - budget.amount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorTokens.Error500
                )
            }
        }
    }
}
```

---

## Component Mapping

### New Components Required

| Component | File | Screens Used |
|-----------|------|--------------|
| `PyeraCard` | `PyeraCard.kt` | All screens |
| `MoneyDisplay` | `MoneyDisplay.kt` | Dashboard, Accounts, Budget, Transactions |
| `TransactionListItem` | `TransactionListItem.kt` | Dashboard, Transaction List |
| `BudgetCard` | `BudgetCard.kt` | Budget, Dashboard |
| `SectionHeader` | `SectionHeader.kt` | Dashboard, Budget, Settings |
| `QuickActionButton` | `QuickActionButton.kt` | Dashboard |
| `EmptyState` | `EmptyState.kt` | All list screens |
| `MetricItem` | `MetricItem.kt` | Dashboard, Analysis |

### Icon Mapping (Phosphor Icons)

| Old Icon | New Phosphor Icon | Screen |
|----------|-------------------|--------|
| `Icons.Default.Home` | `PhosphorIcons.Regular.House` | Bottom Nav |
| `Icons.Default.List` | `PhosphorIcons.Regular.ArrowLeftRight` | Bottom Nav |
| `Icons.Default.PieChart` | `PhosphorIcons.Regular.ChartPieSlice` | Bottom Nav |
| `Icons.Default.Person` | `PhosphorIcons.Regular.User` | Bottom Nav |
| `Icons.Default.Add` | `PhosphorIcons.Regular.Plus` | FAB |
| `Icons.Default.Search` | `PhosphorIcons.Regular.MagnifyingGlass` | App Bar |
| `Icons.Default.Filter` | `PhosphorIcons.Regular.Funnel` | App Bar |
| `Icons.Default.ArrowUpward` | `PhosphorIcons.Regular.ArrowUp` | Income |
| `Icons.Default.ArrowDownward` | `PhosphorIcons.Regular.ArrowDown` | Expense |

---

## Implementation Priority

### Phase 1: Foundation (Week 1)
- ✅ Token system (already done)
- ✅ Core components (already done)
- [ ] Update Theme.kt integration
- [ ] Add Phosphor Icons dependency

### Phase 2: Auth & Core (Week 2)
1. **LoginScreen.kt** - High priority, user first impression
2. **RegisterScreen.kt** - High priority, user onboarding
3. **DashboardScreen.kt** - Critical, main app entry
4. **MainActivity.kt** - Navigation theming

### Phase 3: Transaction Flow (Week 3)
5. **TransactionListScreen.kt** - Critical, most used
6. **AddTransactionScreen.kt** - Critical, daily use
7. **TransactionViewModel.kt** - Update if needed

### Phase 4: Financial Features (Week 4)
8. **BudgetScreen.kt** - High priority
9. **AccountsScreen.kt** - High priority
10. **TransferScreen.kt** - Medium priority
11. **BudgetDetailScreen.kt** - Medium priority

### Phase 5: Secondary Features (Week 5)
12. **DebtScreen.kt** - Medium priority
13. **SavingsScreen.kt** - Medium priority
14. **AnalysisScreen.kt** - Medium priority
15. **InsightsScreen.kt** - Medium priority

### Phase 6: Settings & Polish (Week 6)
16. **ProfileScreen.kt** - Low priority
17. **ThemeSettingsScreen.kt** - Low priority
18. **SecuritySettingsScreen.kt** - Medium priority
19. **AppLockScreen.kt** - High priority (security)
20. **SetPinScreen.kt** - High priority (security)
21. **InvestmentsScreen.kt** - Low priority
22. **BillsScreen.kt** - Low priority
23. **ChatScreen.kt** - Low priority

---

## Migration Strategy

### Step-by-Step Migration

1. **Backup Current Code**
   ```bash
   git checkout -b feature/ui-redesign
   git add .
   git commit -m "Backup before redesign"
   ```

2. **Update Dependencies**
   ```kotlin
   // Add to app/build.gradle.kts
   implementation("com.phosphor:phosphor-compose:1.0.0")
   ```

3. **Migrate Screens in Priority Order**
   - Start with Login (isolated, easy to test)
   - Then Dashboard (showcases new design)
   - Then Transaction flow (most used)
   - Continue down priority list

4. **Testing After Each Screen**
   - Visual inspection
   - Dark/light mode toggle
   - Accessibility check
   - Performance check

5. **Final Integration Testing**
   - End-to-end flows
   - Navigation transitions
   - State persistence

---

## Quality Checklist per Screen

Before marking screen as complete:

- [ ] Uses `ColorTokens` for all colors
- [ ] Uses `SpacingTokens` for all spacing
- [ ] Uses `PyeraCard` instead of `Card`
- [ ] Uses `MoneyDisplay` for all monetary values
- [ ] Uses Phosphor Icons
- [ ] Proper Material 3 components
- [ ] Dark mode tested
- [ ] Light mode tested
- [ ] Accessibility labels added
- [ ] Touch targets 48dp+
- [ ] No hardcoded values
- [ ] Matches Figma specs

---

## Estimates

| Phase | Screens | Est. Time | Team Size |
|-------|---------|-----------|-----------|
| Phase 1 | Foundation | 2 days | 1 dev |
| Phase 2 | Auth & Core | 3 days | 2 devs |
| Phase 3 | Transaction | 3 days | 2 devs |
| Phase 4 | Financial | 3 days | 2 devs |
| Phase 5 | Secondary | 3 days | 1 dev |
| Phase 6 | Settings | 3 days | 1 dev |
| **Total** | **22 screens** | **17 days** | **2-3 devs** |

---

## Success Criteria

All screens must:
1. ✅ Use new design tokens exclusively
2. ✅ Pass visual QA (match Figma)
3. ✅ Pass accessibility audit
4. ✅ Work in both light/dark mode
5. ✅ Maintain existing functionality
6. ✅ Have no performance regressions

---

**Ready to begin implementation! Start with Phase 1.**
