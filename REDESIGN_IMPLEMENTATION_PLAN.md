# Pyera Finance - UI/UX Redesign Implementation Plan

**Version:** 2.0  
**Date:** February 2026  
**Status:** Expert Design System Overhaul  

---

## Executive Summary

Based on comprehensive UI audit and 2024-2025 design trend research, this plan outlines a complete visual redesign of Pyera Finance. The redesign moves from the current "Nocturne Ember" aesthetic to a modern, sophisticated "Midnight Slate" design language that emphasizes clarity, trust, and premium feel while maintaining excellent accessibility.

### Key Changes
- **Color System:** Shift from warm ember to cool slate with gold accents
- **Typography:** Adopt Inter variable font for superior readability
- **Components:** Unify 3 card variants into single, refined component
- **Interactions:** Add micro-animations and haptic feedback
- **Accessibility:** WCAG 2.1 Level AA compliance throughout

---

## Phase 1: Foundation (Week 1-2)

### 1.1 Design Token System

Create comprehensive design tokens in `ui/theme/tokens/`:

```kotlin
// ColorTokens.kt
object ColorTokens {
    // Primary Palette - Trust Blue
    val Primary50 = Color(0xFFEEF4FF)
    val Primary100 = Color(0xFFD9E6FF)
    val Primary500 = Color(0xFF3D5AFE)  // Main brand color
    val Primary600 = Color(0xFF304FFE)
    val Primary900 = Color(0xFF1A237E)
    
    // Secondary - Growth Green
    val Success500 = Color(0xFF00C853)
    val Success600 = Color(0xFF00B248)
    
    // Semantic
    val Error500 = Color(0xFFFF5252)
    val Warning500 = Color(0xFFFFB300)
    val Info500 = Color(0xFF448AFF)
    
    // Neutral - Midnight Slate
    val Slate50 = Color(0xFFF8FAFC)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate400 = Color(0xFF94A3B8)
    val Slate600 = Color(0xFF475569)
    val Slate800 = Color(0xFF1E293B)
    val Slate900 = Color(0xFF0F172A)  // Main background
    val Slate950 = Color(0xFF020617)  // Deepest background
    
    // Surface Elevations (Material 3)
    val SurfaceLevel0 = Slate950
    val SurfaceLevel1 = Color(0xFF0F172A)  // Cards
    val SurfaceLevel2 = Color(0xFF1E293B)  // Elevated cards
    val SurfaceLevel3 = Color(0xFF334155)  // Dialogs, menus
}

// TypeTokens.kt  
object TypeTokens {
    val FontFamily = InterFontFamily  // Variable font
    
    // Material 3 Type Scale with customizations
    val DisplayLarge = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Light,  // 300
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    )
    
    val DisplayMedium = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    )
    
    // Money display - tabular figures for alignment
    val MoneyLarge = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.SemiBold,  // 600
        fontSize = 40.sp,
        lineHeight = 48.sp,
        fontFeatureSettings = "tnum"  // Tabular numbers
    )
    
    val MoneyMedium = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontFeatureSettings = "tnum"
    )
}

// SpacingTokens.kt
object SpacingTokens {
    val None = 0.dp
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val ExtraLarge = 32.dp
    val XXL = 48.dp
    val XXXL = 64.dp
}

// RadiusTokens.kt
object RadiusTokens {
    val None = 0.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val Full = 1000.dp  // For circles/pills
}

// ElevationTokens.kt
object ElevationTokens {
    val Level0 = 0.dp
    val Level1 = 1.dp   // Cards
    val Level2 = 3.dp   // Elevated cards
    val Level3 = 6.dp   // FAB, dialogs
    val Level4 = 8.dp   // Navigation drawer
}
```

### 1.2 New Theme Configuration

```kotlin
// Theme.kt - Complete overhaul
@Composable
fun PyeraTheme(
    darkTheme: Boolean = true,  // Default to dark
    dynamicColor: Boolean = false,  // Disable dynamic for brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    // Configure status bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PyeraTypography,
        shapes = PyeraShapes,
        content = content
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = ColorTokens.Primary500,
    onPrimary = Color.White,
    primaryContainer = ColorTokens.Primary900,
    onPrimaryContainer = ColorTokens.Primary100,
    
    secondary = ColorTokens.Success500,
    onSecondary = Color.Black,
    
    surface = ColorTokens.SurfaceLevel1,
    onSurface = ColorTokens.Slate100,
    surfaceVariant = ColorTokens.SurfaceLevel2,
    onSurfaceVariant = ColorTokens.Slate400,
    
    background = ColorTokens.SurfaceLevel0,
    onBackground = ColorTokens.Slate100,
    
    error = ColorTokens.Error500,
    outline = ColorTokens.Slate600,
    
    // Custom colors
    surfaceTint = ColorTokens.Primary500
)
```

### 1.3 Icon System Overhaul

Replace inconsistent icons with **Phosphor Icons** (consistent 2dp stroke):

```kotlin
// Icons.kt - Centralized icon definitions
object PyeraIcons {
    // Navigation
    val Home = PhosphorIcons.Regular.House
    val Transactions = PhosphorIcons.Regular.ArrowLeftRight
    val Budget = PhosphorIcons.Regular.ChartPieSlice
    val Profile = PhosphorIcons.Regular.User
    
    // Actions
    val Add = PhosphorIcons.Regular.Plus
    val Scan = PhosphorIcons.Regular.Scanner
    val Transfer = PhosphorIcons.Regular.ArrowRight
    val Search = PhosphorIcons.Regular.MagnifyingGlass
    
    // Transaction types
    val Income = PhosphorIcons.Regular.ArrowUp
    val Expense = PhosphorIcons.Regular.ArrowDown
    
    // Categories (sample)
    val Food = PhosphorIcons.Regular.Hamburger
    val Transport = PhosphorIcons.Regular.Car
    val Shopping = PhosphorIcons.Regular.ShoppingBag
    
    // Filled variants for active states
    val HomeFilled = PhosphorIcons.Fill.House
    val TransactionsFilled = PhosphorIcons.Fill.ArrowLeftRight
}
```

---

## Phase 2: Component Library (Week 2-3)

### 2.1 Unified Card Component

Replace 3 card variants with single refined component:

```kotlin
@Composable
fun PyeraCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Default,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val (background, border, elevation) = when (variant) {
        CardVariant.Default -> Triple(
            ColorTokens.SurfaceLevel1,
            BorderStroke(1.dp, ColorTokens.Slate800),
            ElevationTokens.Level1
        )
        CardVariant.Elevated -> Triple(
            ColorTokens.SurfaceLevel2,
            null,
            ElevationTokens.Level2
        )
        CardVariant.Outlined -> Triple(
            Color.Transparent,
            BorderStroke(1.dp, ColorTokens.Slate700),
            ElevationTokens.Level0
        )
    }
    
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = background),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.Medium),
            content = content
        )
    }
}

enum class CardVariant { Default, Elevated, Outlined }
```

### 2.2 Money Display Component

Create specialized component for financial amounts:

```kotlin
@Composable
fun MoneyDisplay(
    amount: Double,
    currency: String = "PHP",
    size: MoneySize = MoneySize.Medium,
    isPositive: Boolean? = null,  // null for neutral (gray)
    showSign: Boolean = false,
    animate: Boolean = false
) {
    val formattedAmount = formatCurrency(amount, currency)
    val (whole, decimal) = formattedAmount.split(".")
    
    val color = when {
        isPositive == true -> ColorTokens.Success500
        isPositive == false -> ColorTokens.Error500
        else -> LocalContentColor.current
    }
    
    val (textStyle, decimalStyle) = when (size) {
        MoneySize.Large -> TypeTokens.MoneyLarge to TypeTokens.MoneyLarge.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp
        )
        MoneySize.Medium -> TypeTokens.MoneyMedium to TypeTokens.MoneyMedium.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp
        )
        MoneySize.Small -> MaterialTheme.typography.titleMedium to 
            MaterialTheme.typography.titleSmall
    }
    
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.animateContentSize()
    ) {
        if (showSign) {
            Text(
                text = if (amount >= 0) "+" else "−",
                style = textStyle,
                color = color,
                modifier = Modifier.padding(end = SpacingTokens.ExtraSmall)
            )
        }
        
        Text(
            text = currencySymbol(currency),
            style = textStyle.copy(fontWeight = FontWeight.Normal),
            color = color.copy(alpha = 0.7f)
        )
        
        Text(
            text = whole,
            style = textStyle,
            color = color
        )
        
        Text(
            text = ".$decimal",
            style = decimalStyle,
            color = color.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}
```

### 2.3 Transaction List Item Redesign

Modern swipeable list item with contextual actions:

```kotlin
@Composable
fun TransactionListItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> {
                    onDelete()
                    true
                }
                DismissValue.DismissedToEnd -> {
                    onEdit()
                    true
                }
                else -> false
            }
        }
    )
    
    SwipeToDismiss(
        state = dismissState,
        background = { SwipeBackground(dismissState) },
        dismissContent = {
            PyeraCard(
                variant = CardVariant.Default,
                onClick = onClick,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Icon + Category
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryIcon(
                            category = transaction.category,
                            size = IconSize.Large
                        )
                        
                        Spacer(modifier = Modifier.width(SpacingTokens.Medium))
                        
                        Column {
                            Text(
                                text = transaction.note.ifBlank { transaction.categoryName },
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatRelativeDate(transaction.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Right: Amount
                    MoneyDisplay(
                        amount = transaction.amount,
                        isPositive = transaction.type == "INCOME",
                        size = MoneySize.Small
                    )
                }
            }
        }
    )
}

@Composable
private fun SwipeBackground(dismissState: DismissState) {
    val direction = dismissState.dismissDirection
    val color = when (direction) {
        DismissDirection.StartToEnd -> ColorTokens.Info500  // Edit
        DismissDirection.EndToStart -> ColorTokens.Error500  // Delete
        else -> Color.Transparent
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color, shape = RoundedCornerShape(RadiusTokens.Large))
            .padding(SpacingTokens.Medium),
        contentAlignment = if (direction == DismissDirection.StartToEnd) 
            Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Icon(
            imageVector = if (direction == DismissDirection.StartToEnd) 
                PyeraIcons.Edit else PyeraIcons.Delete,
            contentDescription = null,
            tint = Color.White
        )
    }
}
```

### 2.4 Bottom Navigation Redesign

Modern bottom nav with pill-shaped active indicator:

```kotlin
@Composable
fun PyeraBottomNavigation(
    navController: NavController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        containerColor = ColorTokens.SurfaceLevel1.copy(alpha = 0.95f),
        tonalElevation = ElevationTokens.Level3,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = { /* Navigation logic */ },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ColorTokens.Primary500,
                    selectedTextColor = ColorTokens.Primary500,
                    indicatorColor = ColorTokens.Primary500.copy(alpha = 0.15f)
                )
            )
        }
    }
}
```

---

## Phase 3: Screen Redesigns (Week 3-5)

### 3.1 Dashboard Redesign

Hero-focused layout with quick actions:

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
            // Hero Section - Total Balance
            HeroBalanceCard(
                totalBalance = uiState.totalBalance,
                monthlyIncome = uiState.monthlyIncome,
                monthlyExpense = uiState.monthlyExpense,
                modifier = Modifier.padding(SpacingTokens.Medium)
            )
            
            // Quick Actions - Horizontal scroll
            QuickActionsRow(
                onAddIncome = { /* Navigate */ },
                onAddExpense = { /* Navigate */ },
                onScan = { /* Navigate */ },
                onTransfer = { /* Navigate */ },
                modifier = Modifier.padding(vertical = SpacingTokens.Medium)
            )
            
            // Recent Transactions
            SectionHeader(
                title = "Recent Transactions",
                action = "See All",
                onActionClick = { /* Navigate to list */ },
                modifier = Modifier.padding(horizontal = SpacingTokens.Medium)
            )
            
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .padding(horizontal = SpacingTokens.Medium),
                userScrollEnabled = false  // Parent scrolls
            ) {
                items(
                    items = uiState.recentTransactions.take(5),
                    key = { it.id }
                ) { transaction ->
                    TransactionListItem(
                        transaction = transaction,
                        onClick = { /* Navigate to detail */ },
                        onEdit = { /* Show edit */ },
                        onDelete = { /* Confirm delete */ }
                    )
                    
                    if (transaction != uiState.recentTransactions.take(5).last()) {
                        Spacer(modifier = Modifier.height(SpacingTokens.Small))
                    }
                }
            }
            
            // Budget Preview
            if (uiState.activeBudgets.isNotEmpty()) {
                SectionHeader(
                    title = "Active Budgets",
                    modifier = Modifier.padding(SpacingTokens.Medium)
                )
                
                BudgetPreviewCarousel(
                    budgets = uiState.activeBudgets,
                    modifier = Modifier.padding(horizontal = SpacingTokens.Medium)
                )
            }
        }
    }
}

@Composable
private fun HeroBalanceCard(
    totalBalance: Double,
    monthlyIncome: Double,
    monthlyExpense: Double,
    modifier: Modifier = Modifier
) {
    PyeraCard(
        variant = CardVariant.Elevated,
        modifier = modifier.fillMaxWidth()
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
            AnimatedMoneyDisplay(
                amount = totalBalance,
                size = MoneySize.Large
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.Large))
            
            // Income/Expense row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    icon = PyeraIcons.Income,
                    label = "Income",
                    amount = monthlyIncome,
                    color = ColorTokens.Success500
                )
                
                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outline
                )
                
                MetricItem(
                    icon = PyeraIcons.Expense,
                    label = "Expense",
                    amount = monthlyExpense,
                    color = ColorTokens.Error500
                )
            }
        }
    }
}
```

### 3.2 Transaction List Redesign

Modern filterable list with sticky headers:

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
                    IconButton(onClick = { /* Show filter bottom sheet */ }) {
                        Icon(PyeraIcons.Filter, contentDescription = "Filter")
                    }
                    IconButton(onClick = { /* Show search */ }) {
                        Icon(PyeraIcons.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Navigate to add */ },
                icon = { Icon(PyeraIcons.Add, null) },
                text = { Text("Add") }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(padding)
        ) {
            if (uiState.groupedTransactions.isEmpty()) {
                EmptyTransactionsState()
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(SpacingTokens.Medium)
                ) {
                    uiState.groupedTransactions.forEach { (date, transactions) ->
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
```

### 3.3 Budget Screen Redesign

Visual progress-focused design:

```kotlin
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("Budgets") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Create budget */ }) {
                Icon(PyeraIcons.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(SpacingTokens.Medium)
        ) {
            // Summary card at top
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
private fun BudgetCard(
    budget: BudgetWithSpending,
    onClick: () -> Unit
) {
    val progress = (budget.spentAmount / budget.amount).toFloat().coerceIn(0f, 1f)
    val isOverBudget = budget.spentAmount > budget.amount
    
    val progressColor = when {
        isOverBudget -> ColorTokens.Error500
        progress > 0.8f -> ColorTokens.Warning500
        else -> ColorTokens.Success500
    }
    
    PyeraCard(onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Category + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIcon(budget.category, IconSize.Medium)
                    Spacer(modifier = Modifier.width(SpacingTokens.Small))
                    Text(
                        text = budget.categoryName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
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
                    .height(8.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Full)),
                color = progressColor,
                trackColor = ColorTokens.Slate800
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.Small))
            
            // Footer: Spent / Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverBudget) ColorTokens.Error500 else progressColor
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
                    text = "Over budget by ${formatCurrency(budget.spentAmount - budget.amount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorTokens.Error500
                )
            }
        }
    }
}
```

### 3.4 Auth Screens Redesign

Clean, focused authentication flow:

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
        // Subtle gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ColorTokens.Primary900.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(0.5f, 0.3f),
                        radius = 800f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpacingTokens.Large)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Logo
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "Pyera",
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))
            
            // Title
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.XXL))
            
            // Form
            LoginForm(
                email = uiState.email,
                onEmailChange = viewModel::onEmailChange,
                password = uiState.password,
                onPasswordChange = viewModel::onPasswordChange,
                isLoading = uiState.isLoading,
                onSubmit = { viewModel.login() },
                error = uiState.error
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.Large))
            
            // Alternative auth
            OrDivider()
            
            Spacer(modifier = Modifier.height(SpacingTokens.Large))
            
            GoogleSignInButton(
                onClick = { viewModel.signInWithGoogle() }
            )
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            // Sign up link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { /* Navigate to register */ }) {
                    Text("Sign Up")
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    onSubmit: () -> Unit,
    error: String?
) {
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(PyeraIcons.Email, null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
        
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            leadingIcon = { Icon(PyeraIcons.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { /* Toggle visibility */ }) {
                    Icon(PyeraIcons.Eye, null)
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (error != null) {
            Spacer(modifier = Modifier.height(SpacingTokens.Small))
            Text(
                text = error,
                color = ColorTokens.Error500,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(SpacingTokens.Large))
        
        Button(
            onClick = onSubmit,
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
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
```

---

## Phase 4: Animations & Interactions (Week 5-6)

### 4.1 Micro-interactions

```kotlin
// AnimatedMoneyDisplay.kt
@Composable
fun AnimatedMoneyDisplay(
    amount: Double,
    size: MoneySize = MoneySize.Medium,
    durationMillis: Int = 800
) {
    val animatedAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "money"
    )
    
    MoneyDisplay(
        amount = animatedAmount.toDouble(),
        size = size
    )
}

// Success animation for transactions
@Composable
fun SuccessCheckAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = ColorTokens.Success500,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Fill.CheckCircle,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Skeleton loading
@Composable
fun SkeletonCard() {
    PyeraCard(variant = CardVariant.Default) {
        Column(modifier = Modifier.padding(SpacingTokens.Medium)) {
            // Title skeleton
            SkeletonBox(width = 120.dp, height = 20.dp)
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            // Amount skeleton
            SkeletonBox(width = 80.dp, height = 32.dp)
        }
    }
}

@Composable
private fun SkeletonBox(width: Dp, height: Dp) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    
    Box(
        modifier = Modifier
            .size(width, height)
            .clip(RoundedCornerShape(RadiusTokens.Small))
            .background(ColorTokens.Slate800)
            .shimmer(shimmer)
    )
}
```

### 4.2 Page Transitions

```kotlin
// Navigation animations
@Composable
fun PyeraNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Navigation graph
    }
}
```

---

## Phase 5: Polish & Accessibility (Week 6)

### 5.1 Accessibility Audit Checklist

- [ ] **Touch Targets**: All interactive elements minimum 48x48dp
- [ ] **Content Descriptions**: All icons and images have descriptions
- [ ] **Focus Order**: Logical tab order through screens
- [ ] **Color Contrast**: WCAG 2.1 Level AA (4.5:1 for text, 3:1 for large text)
- [ ] **Dynamic Type**: All text scales with system settings
- [ ] **Screen Reader**: Test with TalkBack, all information announced
- [ ] **Reduce Motion**: Respect system "Remove animations" setting

### 5.2 Final QA Checklist

- [ ] All screens match Figma designs pixel-perfectly
- [ ] Animations run at 60fps on mid-range devices
- [ ] Dark mode tested thoroughly
- [ ] All strings extracted to resources
- [ ] No hardcoded colors or dimensions
- [ ] ProGuard/R8 keeps necessary classes
- [ ] Memory leak check completed

---

## Implementation Timeline

| Week | Phase | Deliverables |
|------|-------|--------------|
| 1-2 | Foundation | Design tokens, new theme, icon system |
| 2-3 | Components | Card, MoneyDisplay, TransactionItem, BottomNav |
| 3-4 | Core Screens | Dashboard, Transaction List, Budget |
| 4-5 | Secondary Screens | Auth, Accounts, Debt, Savings, Settings |
| 5-6 | Animations | Micro-interactions, transitions, skeleton loading |
| 6 | Polish | Accessibility audit, performance optimization, QA |

**Total Duration: 6 weeks**

---

## Dependencies to Add

```kotlin
// build.gradle.kts

// Material Icons Extended (if not already present)
implementation("androidx.compose.material:material-icons-extended")

// Animation
implementation("androidx.compose.animation:animation:1.6.0")

// Shimmer loading
implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")

// Phosphor Icons (custom library or copy SVGs)
// Option 1: Copy SVG resources
// Option 2: Use phosphor-compose library if available
```

---

## Design System Documentation

Create comprehensive documentation in `docs/DESIGN_SYSTEM.md`:
- Color palette with hex codes
- Typography scale with sizes
- Spacing system
- Component usage guidelines
- Do's and Don'ts with visual examples

---

**This redesign transforms Pyera Finance into a modern, premium financial app that users will trust and enjoy using daily.**
