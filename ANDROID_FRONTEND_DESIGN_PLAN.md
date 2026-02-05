# Pyera Finance - Android Frontend Design Expert Implementation Plan

**Created by:** Android Frontend Design Expert  
**Date:** February 5, 2026  
**Project:** Pyera Finance - Personal Finance Android App  

---

## Executive Summary

This implementation plan addresses critical UI/UX issues identified in the comprehensive audit, fixes existing build errors, and establishes a robust design system for the Pyera Finance Android app. The plan is structured in 6 phases, progressing from foundational fixes to advanced UI polish.

### Current State Assessment

| Category | Status | Issues |
|----------|--------|--------|
| Build Stability | 🔴 Critical | 20+ compilation errors (R references, missing imports) |
| Design System | 🟡 Inconsistent | Hardcoded colors, mixed typography, no spacing system |
| Navigation | 🟡 Overcrowded | 6 bottom nav items, duplicate icons |
| Screen Completion | 🟡 Partial | Profile is placeholder, Auth lacks polish |
| Accessibility | 🔴 Poor | Missing content descriptions, contrast issues |

---

## Phase 1: Foundation & Build Stability (Week 1)

### Objective
Fix all compilation errors and establish a solid foundation for UI development.

### 1.1 Fix Build Errors (Parallel Tasks)

#### Task 1.1.1: Font & Resource Fixes
**Files:**
- `app/src/main/java/com/pyera/app/ui/theme/Type.kt`
- `app/src/main/res/font/` (verify/create font XML files)

**Actions:**
1. Verify `ibrand.xml` and `outfit.xml` font files exist
2. Create font XML files if missing
3. Fix R import references
4. Ensure proper package namespace

**Success Criteria:**
- [ ] Type.kt compiles without errors
- [ ] Font resources properly configured

#### Task 1.1.2: Repository Fixes
**Files:**
- `app/src/main/java/com/pyera/app/data/repository/BudgetRepositoryImpl.kt`
- `app/src/main/java/com/pyera/app/data/repository/ChatRepositoryImpl.kt`

**Actions:**
```kotlin
// Add missing import to BudgetRepositoryImpl.kt
import kotlinx.coroutines.flow.flow

// Fix BuildConfig reference in ChatRepositoryImpl.kt
// Verify proper import: import com.pyera.app.BuildConfig
```

**Success Criteria:**
- [ ] BudgetRepositoryImpl.kt compiles
- [ ] ChatRepositoryImpl.kt compiles

#### Task 1.1.3: Budget UI Fixes
**Files:**
- `app/src/main/java/com/pyera/app/ui/budget/BudgetProgressCard.kt`
- `app/src/main/java/com/pyera/app/ui/budget/BudgetScreen.kt`
- `app/src/main/java/com/pyera/app/ui/budget/CreateBudgetScreen.kt`
- `app/src/main/java/com/pyera/app/ui/budget/BudgetListScreen.kt`

**Actions:**
```kotlin
// BudgetProgressCard.kt - Add missing imports
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement

// CreateBudgetScreen.kt - Fix parameter passing
CategorySelectionSection(
    categories = categories,
    expenseCategories = expenseCategories, // Add this
    selectedCategory = selectedCategory,
    onCategorySelected = { selectedCategory = it }
)
```

**Success Criteria:**
- [ ] All Budget screens compile

#### Task 1.1.4: Auth & Welcome Screen Fixes
**Files:**
- `app/src/main/java/com/pyera/app/ui/auth/LoginScreen.kt`
- `app/src/main/java/com/pyera/app/ui/auth/RegisterScreen.kt`
- `app/src/main/java/com/pyera/app/ui/welcome/WelcomeScreen.kt`
- `app/src/main/java/com/pyera/app/ui/onboarding/OnboardingScreen.kt`

**Actions:**
```kotlin
// Ensure this import exists in all auth/welcome files
import com.pyera.app.R
```

**Success Criteria:**
- [ ] All auth/welcome screens compile

#### Task 1.1.5: Profile & Global Components
**Files:**
- `app/src/main/java/com/pyera/app/ui/profile/ProfileViewModel.kt`
- `app/src/main/java/com/pyera/app/ui/components/Dialogs.kt`
- `app/src/main/java/com/pyera/app/ui/components/EmptyStates.kt`
- `app/src/main/java/com/pyera/app/ui/components/LoadingStates.kt`

**Actions:**
1. Fix ProfileViewModel.kt combine type inference:
```kotlin
val state: StateFlow<ProfileState> = combine(
    transactionRepository.getAllTransactions(),
    savingsRepository.getAllSavingsGoals(),
    budgetRepository.getActiveBudgetsForUser(authRepository.currentUser?.uid ?: ""),
    _notificationsEnabled
) { transactions: List<Transaction>, savingsGoals: List<SavingsGoal>, 
    activeBudgets: List<BudgetEntity>, notifications: Boolean ->
    // mapping logic
}.stateIn(...)
```

2. Create ErrorContainer component if missing
3. Verify EmptyStates component exists
4. Verify LoadingStates component exists

**Success Criteria:**
- [ ] ProfileViewModel.kt compiles
- [ ] All global components exist and work

### 1.2 Build Verification
```bash
./gradlew clean
./gradlew assembleDebug
```

**Success Criteria:**
- [ ] Zero compilation errors
- [ ] All R references resolve
- [ ] App installs successfully

---

## Phase 2: Design System Establishment (Week 1-2)

### Objective
Create a comprehensive, consistent design system following Material 3 principles.

### 2.1 Enhanced Color System

**File:** `app/src/main/java/com/pyera/app/ui/theme/Color.kt`

```kotlin
// Primary Brand Colors
val NeonYellow = Color(0xFFD4FF00)
val NeonYellowDark = Color(0xFFB8E600)
val NeonYellowLight = Color(0xFFE0FF33)

// Background Colors
val DarkGreen = Color(0xFF0A0E0D)
val SurfaceDark = Color(0xFF1A1F1D)
val SurfaceElevated = Color(0xFF242927)
val SurfaceOverlay = Color(0xFF2F3633)

// Semantic Colors (Enhanced for Accessibility)
val ColorSuccess = Color(0xFF4CAF50)
val ColorSuccessDark = Color(0xFF388E3C)
val ColorSuccessContainer = Color(0xFF1B2A12)
val ColorError = Color(0xFFFF5252)
val ColorErrorContainer = Color(0xFF2A0A0A)
val ColorWarning = Color(0xFFFFB300)
val ColorWarningContainer = Color(0xFF2A1F00)
val ColorInfo = Color(0xFF448AFF)

// Text Colors (WCAG AA Compliant)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B8B4)  // Improved contrast
val TextTertiary = Color(0xFF8B918F)
val TextDisabled = Color(0xFF5F6663)

// Interactive States
val ColorOverlayPressed = Color(0x1FFFFFFF)
val ColorOverlayHover = Color(0x0FFFFFFF)

// Borders
val ColorBorder = Color(0xFF3A433F)
val ColorBorderFocused = NeonYellow.copy(alpha = 0.5f)

// Currency Colors
val ColorIncome = ColorSuccess
val ColorExpense = ColorError
val ColorDebt = ColorWarning
```

### 2.2 Complete Typography Scale

**File:** `app/src/main/java/com/pyera/app/ui/theme/Type.kt`

```kotlin
val Typography = Typography(
    // Display - Large numbers, hero text
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.2).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.15).sp
    ),
    
    // Headlines - Screen titles
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.1).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.05).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Titles - Card titles, section headers
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body - Main content text
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Labels - Buttons, captions, overlines
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### 2.3 Spacing System

**File:** `app/src/main/java/com/pyera/app/ui/theme/Spacing.kt`

```kotlin
object Spacing {
    val None = 0.dp
    val XXSmall = 2.dp
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val XLarge = 24.dp
    val XXLarge = 32.dp
    val XXXLarge = 48.dp
    
    // Component-specific
    val CardPadding = 16.dp
    val ScreenPadding = 16.dp
    val DialogPadding = 24.dp
    val ListItemSpacing = 12.dp
    val SectionSpacing = 24.dp
}
```

### 2.4 Standardized Component Library

#### 2.4.1 PyeraButton Variants

**File:** `app/src/main/java/com/pyera/app/ui/components/PyeraButton.kt`

```kotlin
@Composable
fun PyeraButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val colors = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = NeonYellow,
            contentColor = DarkGreen,
            disabledContainerColor = NeonYellow.copy(alpha = 0.38f),
            disabledContentColor = DarkGreen.copy(alpha = 0.38f)
        )
        ButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = SurfaceElevated,
            contentColor = TextPrimary
        )
        ButtonVariant.Tertiary -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = NeonYellow
        )
        ButtonVariant.Destructive -> ButtonDefaults.buttonColors(
            containerColor = ColorErrorContainer,
            contentColor = ColorError
        )
    }
    
    val height = when (size) {
        ButtonSize.Small -> 32.dp
        ButtonSize.Medium -> 48.dp
        ButtonSize.Large -> 56.dp
    }
    
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        colors = colors,
        enabled = enabled,
        content = content
    )
}

enum class ButtonVariant { Primary, Secondary, Tertiary, Destructive }
enum class ButtonSize { Small, Medium, Large }
```

#### 2.4.2 PyeraTextField

**File:** `app/src/main/java/com/pyera/app/ui/components/PyeraTextField.kt`

```kotlin
@Composable
fun PyeraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it, color = TextTertiary) } },
        leadingIcon = leadingIcon?.let {
            { Icon(it, contentDescription = null, tint = TextSecondary) }
        },
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = ColorError) }
        } else null,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone?.invoke() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonYellow,
            unfocusedBorderColor = ColorBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = NeonYellow,
            unfocusedLabelColor = TextSecondary,
            errorBorderColor = ColorError,
            errorLabelColor = ColorError
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
```

#### 2.4.3 PyeraCurrencyText

**File:** `app/src/main/java/com/pyera/app/ui/components/PyeraCurrencyText.kt`

```kotlin
@Composable
fun PyeraCurrencyText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    isPositive: Boolean? = null,
    showSign: Boolean = true,
    currencyCode: String = "PHP"
) {
    val color = when (isPositive) {
        true -> ColorIncome
        false -> ColorExpense
        null -> TextPrimary
    }
    
    val sign = when {
        !showSign -> ""
        amount > 0 -> "+"
        amount < 0 -> "-"
        else -> ""
    }
    
    val formattedAmount = formatCurrency(kotlin.math.abs(amount), currencyCode)
    
    Text(
        text = "$sign$formattedAmount",
        style = style,
        color = color,
        modifier = modifier
    )
}

private fun formatCurrency(amount: Double, currencyCode: String): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
    format.currency = Currency.getInstance(currencyCode)
    return format.format(amount)
}
```

#### 2.4.4 PyeraCard

**File:** `app/src/main/java/com/pyera/app/ui/components/PyeraCards.kt` (Enhance existing)

```kotlin
@Composable
fun PyeraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    borderColor: Color = ColorBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) {
                Modifier.clickable(enabled = enabled, onClick = onClick)
            } else Modifier
        )
    
    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceElevated
        ),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}
```

#### 2.4.5 PyeraEmptyState

**File:** `app/src/main/java/com/pyera/app/ui/components/EmptyStates.kt` (Enhance existing)

```kotlin
@Composable
fun PyeraEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(Spacing.Large))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.Small))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(Spacing.XLarge))
            PyeraButton(
                onClick = onAction,
                variant = ButtonVariant.Primary
            ) {
                Text(actionLabel)
            }
        }
    }
}
```

### 2.5 Theme Integration

**File:** `app/src/main/java/com/pyera/app/ui/theme/Theme.kt` (Enhance)

```kotlin
@Composable
fun PyeraTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = NeonYellow,
        onPrimary = DarkGreen,
        primaryContainer = NeonYellow.copy(alpha = 0.15f),
        onPrimaryContainer = NeonYellow,
        secondary = SurfaceElevated,
        onSecondary = TextPrimary,
        background = DarkGreen,
        onBackground = TextPrimary,
        surface = SurfaceDark,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceElevated,
        onSurfaceVariant = TextSecondary,
        error = ColorError,
        onError = TextPrimary,
        errorContainer = ColorErrorContainer,
        outline = ColorBorder
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## Phase 3: Navigation Restructure (Week 2)

### Objective
Fix navigation overcrowding and improve UX with a 5-item bottom navigation.

### 3.1 Revised Navigation Structure

**File:** `app/src/main/java/com/pyera/app/ui/navigation/Screen.kt`

```kotlin
// Bottom Navigation - 5 Items Maximum
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem(
        route = Screen.Main.Dashboard.route,
        title = "Home",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )
    
    object Transactions : BottomNavItem(
        route = Screen.Main.Transactions.route,
        title = "Activity",
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
        selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong
    )
    
    object Budget : BottomNavItem(
        route = Screen.Main.Budget.route,
        title = "Budget",
        icon = Icons.Outlined.AccountBalanceWallet,
        selectedIcon = Icons.Filled.AccountBalanceWallet
    )
    
    object Savings : BottomNavItem(
        route = Screen.Main.Savings.route,
        title = "Savings",
        icon = Icons.Outlined.Savings,
        selectedIcon = Icons.Filled.Savings
    )
    
    object Profile : BottomNavItem(
        route = Screen.Main.Profile.route,
        title = "Profile",
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Filled.Person
    )
}

// Items array for BottomNavigation
val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Transactions,
    BottomNavItem.Budget,
    BottomNavItem.Savings,
    BottomNavItem.Profile
)

// Moved items (accessible via other means)
// - Analysis: Move to Dashboard as a card/quick link OR FAB menu
// - Debt: Move to Transactions screen as a tab
```

### 3.2 Enhanced Bottom Navigation

**File:** `app/src/main/java/com/pyera/app/ui/components/PyeraBottomBar.kt`

```kotlin
@Composable
fun PyeraBottomBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = SurfaceDark,
        contentColor = TextPrimary,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonYellow,
                    selectedTextColor = NeonYellow,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = NeonYellow.copy(alpha = 0.1f)
                )
            )
        }
    }
}
```

### 3.3 Transaction Screen with Debt Tab

**File:** `app/src/main/java/com/pyera/app/ui/transaction/TransactionListScreen.kt`

Restructure to include tabs:
- **Transactions**: All transactions with filters
- **Debt**: Moved from separate navigation item

```kotlin
@Composable
fun ActivityScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Transactions", "Debt")
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = NeonYellow,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonYellow
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content
        when (selectedTab) {
            0 -> TransactionListContent(navController, viewModel)
            1 -> DebtContent(navController) // Navigate to Debt screen content
        }
    }
}
```

---

## Phase 4: Screen-by-Screen Redesign (Week 2-4)

### 4.1 Dashboard Redesign

**File:** `app/src/main/java/com/pyera/app/ui/dashboard/DashboardScreen.kt`

#### Key Improvements:

1. **Balance Card Enhancement:**
```kotlin
@Composable
fun EnhancedBalanceCard(
    balance: Double,
    income: Double,
    expenses: Double,
    modifier: Modifier = Modifier
) {
    PyeraCard(
        modifier = modifier,
        borderColor = NeonYellow.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.XLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Total Balance
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(Spacing.Small))
            
            PyeraCurrencyText(
                amount = balance,
                style = MaterialTheme.typography.displaySmall,
                showSign = false
            )
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            // Income/Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IncomeExpenseIndicator(
                    label = "Income",
                    amount = income,
                    isPositive = true,
                    icon = Icons.Default.ArrowDownward
                )
                
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = ColorBorder
                )
                
                IncomeExpenseIndicator(
                    label = "Expense",
                    amount = expenses,
                    isPositive = false,
                    icon = Icons.Default.ArrowUpward
                )
            }
        }
    }
}
```

2. **Quick Actions Fix:**
```kotlin
@Composable
fun QuickActionsRow(
    onAddTransaction: () -> Unit,
    onScanReceipt: () -> Unit,
    onViewAnalysis: () -> Unit  // Analysis shortcut
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        // Fixed Add button with NeonYellow
        QuickActionButton(
            icon = Icons.Default.Add,
            label = "Add",
            onClick = onAddTransaction,
            containerColor = NeonYellow,
            contentColor = DarkGreen
        )
        
        QuickActionButton(
            icon = Icons.Default.CameraAlt,
            label = "Scan",
            onClick = onScanReceipt,
            containerColor = SurfaceElevated,
            contentColor = TextPrimary
        )
        
        // Analysis moved to quick action
        QuickActionButton(
            icon = Icons.Default.BarChart,
            label = "Analysis",
            onClick = onViewAnalysis,
            containerColor = SurfaceElevated,
            contentColor = TextPrimary
        )
    }
}
```

3. **Recent Transactions with CTA:**
```kotlin
@Composable
fun RecentTransactionsSection(
    transactions: List<Transaction>,
    onViewAll: () -> Unit,
    onAddTransaction: () -> Unit
) {
    Column {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            
            TextButton(onClick = onViewAll) {
                Text("View All", color = NeonYellow)
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.Medium))
        
        // Content
        if (transactions.isEmpty()) {
            PyeraEmptyState(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = "No transactions yet",
                description = "Start tracking your expenses by adding your first transaction",
                actionLabel = "Add Transaction",
                onAction = onAddTransaction
            )
        } else {
            transactions.take(5).forEach { transaction ->
                TransactionListItem(transaction = transaction)
                if (transaction != transactions.last()) {
                    Spacer(modifier = Modifier.height(Spacing.Small))
                }
            }
        }
    }
}
```

### 4.2 Auth Screens Redesign

**Files:**
- `app/src/main/java/com/pyera/app/ui/auth/LoginScreen.kt`
- `app/src/main/java/com/pyera/app/ui/auth/RegisterScreen.kt`

#### Login Screen Improvements:

```kotlin
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val isFormValid by remember(email, password) {
        derivedStateOf {
            email.isValidEmail() && password.length >= 6
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.XXXLarge))
        
        // App Logo/Branding
        AppLogo()
        
        Spacer(modifier = Modifier.height(Spacing.XXLarge))
        
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        
        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(Spacing.XLarge))
        
        // Email Field
        PyeraTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        
        Spacer(modifier = Modifier.height(Spacing.Medium))
        
        // Password Field with Visibility Toggle
        PyeraTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.Visibility
                        } else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = TextSecondary
                    )
                }
            },
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else PasswordVisualTransformation(),
            onDone = { if (isFormValid) viewModel.login(email, password) }
        )
        
        // Forgot Password Link
        TextButton(
            onClick = { navController.navigate(Screen.ForgotPassword.route) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot Password?", color = NeonYellow)
        }
        
        Spacer(modifier = Modifier.height(Spacing.Large))
        
        // Login Button
        PyeraButton(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
            enabled = isFormValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = DarkGreen
                )
            } else {
                Text("Sign In")
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.XLarge))
        
        // Social Login Section
        SocialLoginSection()
        
        Spacer(modifier = Modifier.height(Spacing.XLarge))
        
        // Register Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account?", color = TextSecondary)
            TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                Text("Sign Up", color = NeonYellow)
            }
        }
    }
}
```

### 4.3 Profile Screen Implementation

**File:** `app/src/main/java/com/pyera/app/ui/profile/ProfileScreen.kt`

```kotlin
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // User Header
        UserProfileHeader(
            userName = state.userName,
            email = state.email,
            avatarUrl = state.avatarUrl
        )
        
        Spacer(modifier = Modifier.height(Spacing.Large))
        
        // Quick Stats
        QuickStatsRow(
            transactionCount = state.transactionCount,
            savingsGoals = state.savingsGoalsCount,
            activeBudgets = state.activeBudgetsCount
        )
        
        Spacer(modifier = Modifier.height(Spacing.Large))
        
        // Settings Sections
        SettingsSection(title = "Account") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Personal Information",
                onClick = { navController.navigate(Screen.EditProfile.route) }
            )
            SettingsItem(
                icon = Icons.Default.Security,
                title = "Security",
                onClick = { navController.navigate(Screen.Security.route) }
            )
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                trailing = {
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonYellow,
                            checkedTrackColor = NeonYellow.copy(alpha = 0.5f)
                        )
                    )
                }
            )
        }
        
        SettingsSection(title = "Data") {
            SettingsItem(
                icon = Icons.Default.Download,
                title = "Export to CSV",
                onClick = viewModel::exportData
            )
            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Backup & Restore",
                onClick = { navController.navigate(Screen.Backup.route) }
            )
        }
        
        SettingsSection(title = "App") {
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Appearance",
                subtitle = "Dark mode",
                onClick = { navController.navigate(Screen.Appearance.route) }
            )
            SettingsItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                onClick = { navController.navigate(Screen.Support.route) }
            )
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "Version ${state.appVersion}",
                onClick = { navController.navigate(Screen.About.route) }
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.Large))
        
        // Logout Button
        PyeraButton(
            onClick = viewModel::logout,
            variant = ButtonVariant.Destructive,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenPadding)
        ) {
            Text("Logout")
        }
        
        Spacer(modifier = Modifier.height(Spacing.XXXLarge))
    }
}
```

### 4.4 Transaction List Enhancement

**File:** `app/src/main/java/com/pyera/app/ui/transaction/TransactionListScreen.kt`

```kotlin
@Composable
fun TransactionListScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = viewModel::refresh
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        PyeraTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.search(it)
            },
            placeholder = "Search transactions...",
            leadingIcon = Icons.Default.Search,
            modifier = Modifier.padding(Spacing.ScreenPadding)
        )
        
        // Filter Chips
        FilterChipsRow(
            selected = selectedFilter,
            onSelected = { 
                selectedFilter = it
                viewModel.filter(it)
            }
        )
        
        // Transaction List
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                // Group by date
                val grouped = state.transactions.groupBy { it.date.toLocalDate() }
                
                grouped.forEach { (date, transactions) ->
                    item {
                        DateHeader(date = date)
                    }
                    
                    items(
                        items = transactions,
                        key = { it.id }
                    ) { transaction ->
                        SwipeableTransactionItem(
                            transaction = transaction,
                            onEdit = { navController.navigate(Screen.EditTransaction.createRoute(it)) },
                            onDelete = viewModel::deleteTransaction
                        )
                    }
                }
            }
            
            PullRefreshIndicator(
                refreshing = state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = NeonYellow
            )
            
            // Empty State
            if (state.transactions.isEmpty() && !state.isLoading) {
                PyeraEmptyState(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "No transactions found",
                    description = if (searchQuery.isEmpty()) {
                        "Add your first transaction to get started"
                    } else {
                        "No transactions match your search"
                    },
                    actionLabel = "Add Transaction",
                    onAction = { navController.navigate(Screen.AddTransaction.route) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
```

---

## Phase 5: Global Interactions & Polish (Week 4-5)

### 5.1 Global Snackbar Host

**File:** `app/src/main/java/com/pyera/app/ui/main/MainScreen.kt`

```kotlin
@Composable
fun MainScreen(navController: NavHostController = rememberNavController()) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        bottomBar = { PyeraBottomBar(navController, currentRoute) },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                PyeraSnackbar(data)
            }
        }
    ) { padding ->
        // Navigation content
    }
}

@Composable
fun PyeraSnackbar(data: SnackbarData) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        containerColor = SurfaceElevated,
        contentColor = TextPrimary,
        actionColor = NeonYellow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(data.visuals.message)
    }
}
```

### 5.2 Confirmation Dialogs

**File:** `app/src/main/java/com/pyera/app/ui/components/Dialogs.kt`

```kotlin
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            PyeraButton(
                onClick = onConfirm,
                variant = if (isDestructive) ButtonVariant.Destructive else ButtonVariant.Primary
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            PyeraButton(
                onClick = onDismiss,
                variant = ButtonVariant.Tertiary
            ) {
                Text("Cancel")
            }
        },
        containerColor = SurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
```

### 5.3 Loading States

**File:** `app/src/main/java/com/pyera/app/ui/components/LoadingStates.kt`

```kotlin
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = NeonYellow,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun ShimmerLoadingItem(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        SurfaceElevated,
        SurfaceOverlay,
        SurfaceElevated
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "shimmer"
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
    
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(brush, RoundedCornerShape(12.dp))
    )
}
```

### 5.4 Pull-to-Refresh

Add to all list screens:
- TransactionListScreen
- DebtScreen  
- BudgetScreen
- SavingsScreen

---

## Phase 6: Accessibility & Final Polish (Week 5+)

### 6.1 Accessibility Improvements

1. **Content Descriptions:**
   - Add to all icons: `contentDescription = "..."`
   - Add to images and interactive elements
   - Use `null` only for decorative elements

2. **Touch Targets:**
   - Ensure minimum 48dp touch targets
   - Use `minimumInteractiveComponentSize()` modifier

3. **Semantic Properties:**
```kotlin
Modifier.semantics {
    contentDescription = "Total balance: ${formatCurrency(balance)}"
    stateDescription = if (balance > 0) "Positive" else "Negative"
}
```

4. **Focus Management:**
   - Request focus on first field in forms
   - Handle keyboard navigation properly

### 6.2 Animations

1. **Screen Transitions:**
```kotlin
composable(
    route = Screen.Dashboard.route,
    enterTransition = { fadeIn() + slideInVertically { it / 4 } },
    exitTransition = { fadeOut() + slideOutVertically { it / 4 } }
) {
    DashboardScreen()
}
```

2. **List Item Animations:**
```kotlin
items(
    items = transactions,
    key = { it.id }
) { transaction ->
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
    ) {
        TransactionItem(transaction)
    }
}
```

### 6.3 Haptic Feedback

```kotlin
val haptic = LocalHapticFeedback.current

// On successful action
haptic.performHapticFeedback(HapticFeedbackType.Confirm)

// On error
haptic.performHapticFeedback(HapticFeedbackType.Reject)

// On long press
haptic.performHapticFeedback(HapticFeedbackType.LongPress)
```

---

## Implementation Timeline

| Phase | Duration | Key Deliverables |
|-------|----------|------------------|
| Phase 1 | Week 1 | All build errors fixed, app compiles |
| Phase 2 | Week 1-2 | Complete design system (colors, typography, components) |
| Phase 3 | Week 2 | Navigation restructure (5 items), debt moved to transactions |
| Phase 4 | Week 2-4 | Dashboard, Auth, Profile, Transaction screens redesigned |
| Phase 5 | Week 4-5 | Global components (snackbar, dialogs, loading states) |
| Phase 6 | Week 5+ | Accessibility, animations, haptic feedback, testing |

---

## Success Metrics

| Metric | Current | Target |
|--------|---------|--------|
| Build Status | 20+ errors | 0 errors |
| Color Consistency | 60% | 100% |
| Typography Consistency | 40% | 100% |
| Component Reuse | Low | High |
| Navigation Items | 6 (overcrowded) | 5 (optimal) |
| Screen Completion | 70% | 100% |
| Accessibility Score | 40% | 90%+ |
| WCAG Conformance | AA (partial) | AA (full) |

---

## Code Review Checklist

Before submitting each phase:

- [ ] No hardcoded colors (use theme colors)
- [ ] No hardcoded typography (use MaterialTheme.typography)
- [ ] No hardcoded spacing (use Spacing object)
- [ ] All icons have contentDescription
- [ ] All interactive elements meet 48dp touch target
- [ ] Text contrast ratio meets 4.5:1 minimum
- [ ] Currency formatting consistent (₱ not $)
- [ ] Loading states handled
- [ ] Empty states informative with CTA
- [ ] Error states user-friendly
- [ ] Build compiles without warnings
- [ ] Preview composables added for new components

---

## Resources

- **Project Root:** `c:\coding\Pyera`
- **App Source:** `app/src/main/java/com/pyera/app/`
- **UI Package:** `app/src/main/java/com/pyera/app/ui/`
- **Theme:** `app/src/main/java/com/pyera/app/ui/theme/`
- **Components:** `app/src/main/java/com/pyera/app/ui/components/`

---

*Plan Created by: Android Frontend Design Expert*  
*Date: February 5, 2026*
