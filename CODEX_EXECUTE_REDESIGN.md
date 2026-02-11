# GPT Codex - Complete Screen Redesign Execution Prompt

**Execute this prompt in GPT Codex to implement all 22 screens.**

---

## CONTEXT

You are implementing a complete UI redesign for Pyera Finance, an Android finance app. The redesign transforms the app from "Nocturne Ember" (warm ember/green tones) to "Midnight Slate" (cool slate with trust blue accents).

**Project Location:** `c:\coding\Pyera`
**Package:** `com.pyera.app`
**Framework:** Jetpack Compose + Material 3

---

## DESIGN SYSTEM REFERENCE

### Color Tokens (Use These)
```kotlin
// Primary - Trust Blue
ColorTokens.Primary500 = Color(0xFF3D5AFE)  // Main brand
ColorTokens.Primary600 = Color(0xFF304FFE)  // Pressed

// Semantic
ColorTokens.Success500 = Color(0xFF00C853)  // Income/positive
ColorTokens.Error500 = Color(0xFFFF5252)    // Expense/negative
ColorTokens.Warning500 = Color(0xFFFFB300)  // Warnings

// Surfaces - Midnight Slate
ColorTokens.SurfaceLevel0 = Color(0xFF020617)  // App background
ColorTokens.SurfaceLevel1 = Color(0xFF0F172A)  // Cards
ColorTokens.SurfaceLevel2 = Color(0xFF1E293B)  // Elevated cards
ColorTokens.Slate400 = Color(0xFF94A3B8)       // Secondary text
ColorTokens.Slate600 = Color(0xFF475569)       // Borders
```

### Spacing Tokens
```kotlin
SpacingTokens.ExtraSmall = 4.dp
SpacingTokens.Small = 8.dp
SpacingTokens.Medium = 16.dp      // Most common
SpacingTokens.Large = 24.dp
SpacingTokens.ExtraLarge = 32.dp
```

### Components (Already Created)
```kotlin
// Unified card with 3 variants
PyeraCard(
    variant = CardVariant.Default,   // Border + low elevation
    variant = CardVariant.Elevated,  // No border + higher elevation
    variant = CardVariant.Outlined,  // Border only, no elevation
    onClick = { /* optional */ }
) { /* content */ }

// Animated money display
MoneyDisplay(
    amount = 1234.56,
    size = MoneySize.Large,    // 40sp
    size = MoneySize.Medium,   // 28sp
    size = MoneySize.Small,    // 16sp
    isPositive = true,         // Green color
    isPositive = false,        // Red color
    animate = true             // Count-up animation
)
```

---

## EXECUTION ORDER - FOLLOW STRICTLY

**Execute ONE phase at a time. Confirm completion before proceeding.**

---

## PHASE 1: Update Theme Integration

### Task 1.1: Verify Theme.kt
**File:** `app/src/main/java/com/pyera/app/ui/theme/Theme.kt`

Check that it uses ColorTokens. If not, update it to:
```kotlin
import com.pyera.app.ui.theme.tokens.ColorTokens

private val DarkColorScheme = darkColorScheme(
    primary = ColorTokens.Primary500,
    onPrimary = Color.White,
    background = ColorTokens.SurfaceLevel0,
    surface = ColorTokens.SurfaceLevel1,
    surfaceVariant = ColorTokens.SurfaceLevel2,
    // ... etc
)
```

### Task 1.2: Verify Type.kt
**File:** `app/src/main/java/com/pyera/app/ui/theme/Type.kt`

Ensure it includes money typography:
```kotlin
val MoneyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 40.sp
)
val MoneyMedium = TextStyle(...28.sp)
val MoneySmall = TextStyle(...16.sp)
```

**Stop and verify:** Build the project `./gradlew :app:compileDebugKotlin`

---

## PHASE 2: Authentication Screens (2 screens)

### Screen 1: LoginScreen.kt
**File:** `app/src/main/java/com/pyera/app/ui/auth/LoginScreen.kt`

**Redesign Instructions:**

1. **Update imports:**
```kotlin
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.CardVariant
```

2. **Replace background:**
```kotlin
// OLD
Box(modifier = Modifier.background(DarkGreen))

// NEW
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(ColorTokens.SurfaceLevel0)
) {
    // Add subtle radial gradient
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
}
```

3. **Replace form card:**
```kotlin
// OLD
card(backgroundColor = SurfaceDark) { }

// NEW
PyeraCard(
    variant = CardVariant.Elevated,
    modifier = Modifier.fillMaxWidth()
) {
    Column(
        modifier = Modifier.padding(SpacingTokens.Large)
    ) {
        // Form content
    }
}
```

4. **Update spacing:**
```kotlin
// Replace all hardcoded dp values
16.dp → SpacingTokens.Medium
24.dp → SpacingTokens.Large
32.dp → SpacingTokens.ExtraLarge
```

5. **Update text field styling:**
```kotlin
OutlinedTextField(
    shape = RoundedCornerShape(12.dp),  // Consistent radius
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ColorTokens.Primary500,
        unfocusedBorderColor = ColorTokens.Slate600
    )
)
```

### Screen 2: RegisterScreen.kt
**File:** `app/src/main/java/com/pyera/app/ui/auth/RegisterScreen.kt`

Apply same changes as LoginScreen:
- ✅ Background with gradient
- ✅ Elevated card for form
- ✅ Spacing tokens
- ✅ Updated text field styling

**Stop and verify:** 
- [ ] Both auth screens compile
- [ ] Visual inspection in emulator
- [ ] Dark mode works
- [ ] Light mode works

---

## PHASE 3: Core Dashboard (1 screen)

### Screen 3: DashboardScreen.kt
**File:** `app/src/main/java/com/pyera/app/ui/dashboard/DashboardScreen.kt`

**Redesign Instructions:**

1. **Hero Balance Card:**
```kotlin
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
        
        // Income/Expense row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                icon = Icons.Default.ArrowUpward,
                label = "Income",
                amount = uiState.monthlyIncome,
                color = ColorTokens.Success500
            )
            
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.outline
            )
            
            MetricItem(
                icon = Icons.Default.ArrowDownward,
                label = "Expense",
                amount = uiState.monthlyExpense,
                color = ColorTokens.Error500
            )
        }
    }
}
```

2. **Create MetricItem component:**
```kotlin
@Composable
private fun MetricItem(
    icon: ImageVector,
    label: String,
    amount: Double,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
        MoneyDisplay(amount = amount, size = MoneySize.Small)
    }
}
```

3. **Quick Actions Row:**
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = SpacingTokens.Medium),
    horizontalArrangement = Arrangement.SpaceEvenly
) {
    QuickActionButton(
        icon = Icons.Default.Add,
        label = "Add",
        onClick = { /* Navigate */ }
    )
    // Repeat for Scan, Transfer, Budget
}
```

4. **Recent Transactions:**
```kotlin
SectionHeader(
    title = "Recent Transactions",
    action = "See All",
    onActionClick = { /* Navigate */ }
)

uiState.recentTransactions.take(5).forEach { transaction ->
    TransactionListItem(
        transaction = transaction,
        onClick = { /* Detail */ },
        onEdit = { /* Edit */ },
        onDelete = { /* Delete */ }
    )
    Spacer(modifier = Modifier.height(SpacingTokens.Small))
}
```

**Stop and verify:**
- [ ] Dashboard compiles
- [ ] Balance animates
- [ ] Money displays correctly
- [ ] Cards have consistent styling

---

## PHASE 4: Transaction Flow (3 screens)

### Screen 4: TransactionListScreen.kt
**File:** `app/src/main/java/com/pyera/app/ui/transaction/TransactionListScreen.kt`

**Redesign Instructions:**

1. **Top App Bar:**
```kotlin
TopAppBar(
    title = { Text("Transactions") },
    actions = {
        IconButton(onClick = { /* Search */ }) {
            Icon(Icons.Default.Search, "Search")
        }
        IconButton(onClick = { /* Filter */ }) {
            Icon(Icons.Default.FilterList, "Filter")
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = ColorTokens.SurfaceLevel0
    )
)
```

2. **Sticky Date Headers:**
```kotlin
LazyColumn {
    groupedTransactions.forEach { (date, transactions) ->
        stickyHeader(key = date) {
            Surface(
                color = ColorTokens.SurfaceLevel0.copy(alpha = 0.95f)
            ) {
                Text(
                    text = date,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        items(transactions, key = { it.id }) { transaction ->
            TransactionListItem(
                transaction = transaction,
                onClick = { /* Detail */ },
                onEdit = { /* Edit */ },
                onDelete = { /* Delete */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
```

### Screen 5: AddTransactionScreen.kt
**File:** `app/src/main/java/com/pyera/app/ui/transaction/AddTransactionScreen.kt`

**Redesign Instructions:**

1. **Form Layout:**
```kotlin
PyeraCard(
    variant = CardVariant.Default,
    modifier = Modifier.fillMaxWidth()
) {
    Column(
        modifier = Modifier.padding(SpacingTokens.Large)
    ) {
        // Amount field (prominent)
        OutlinedTextField(
            value = amount,
            onValueChange = { /* */ },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
        
        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { /* */ },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // ... other fields
    }
}
```

### Screen 6: TransactionDetailScreen.kt (if exists)
Apply same patterns: PyeraCard, spacing tokens, updated colors.

**Stop and verify:**
- [ ] All 3 transaction screens compile
- [ ] List shows with sticky headers
- [ ] Add transaction form works
- [ ] Money displays animate

---

## PHASE 5: Budget Screens (2 screens)

### Screen 7: BudgetScreen.kt
**File:** `app/src/main/java/com/pyera/app/ui/budget/BudgetScreen.kt`

**Redesign Instructions:**

1. **Budget Summary Card:**
```kotlin
PyeraCard(variant = CardVariant.Elevated) {
    Column(modifier = Modifier.padding(SpacingTokens.Large)) {
        Text("Monthly Overview", style = MaterialTheme.typography.titleMedium)
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total Budget", style = MaterialTheme.typography.bodySmall)
                MoneyDisplay(amount = totalBudget, size = MoneySize.Medium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Spent", style = MaterialTheme.typography.bodySmall)
                MoneyDisplay(
                    amount = totalSpent,
                    size = MoneySize.Medium,
                    isPositive = totalSpent <= totalBudget
                )
            }
        }
        
        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when {
                progress > 1f -> ColorTokens.Error500
                progress > 0.8f -> ColorTokens.Warning500
                else -> ColorTokens.Success500
            },
            trackColor = ColorTokens.Slate800
        )
    }
}
```

2. **Budget Card:**
```kotlin
@Composable
fun BudgetCard(budget: Budget) {
    PyeraCard(onClick = { /* Navigate */ }) {
        Column(modifier = Modifier.padding(SpacingTokens.Medium)) {
            // Category + remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    CategoryIcon(budget.category)
                    Text(budget.categoryName)
                }
                MoneyDisplay(
                    amount = budget.remaining,
                    isPositive = !budget.isOverBudget
                )
            }
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { budget.progress },
                color = budget.progressColor,
                trackColor = ColorTokens.Slate800
            )
            
            // Footer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${budget.percentage}% used")
                Text("${budget.spent} of ${budget.total}")
            }
        }
    }
}
```

### Screen 8: BudgetDetailScreen.kt
Apply same patterns as BudgetScreen with more details.

**Stop and verify:**
- [ ] Budget screens compile
- [ ] Progress bars show correct colors
- [ ] Money displays work

---

## PHASE 6: Accounts & Transfer (3 screens)

### Screen 9: AccountsScreen.kt
**File:** `app/src/main/java/com/pyera/app/ui/account/AccountsScreen.kt`

**Redesign Instructions:**

1. **Total Balance Header:**
```kotlin
PyeraCard(variant = CardVariant.Elevated) {
    Column(
        modifier = Modifier.padding(SpacingTokens.Large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Total Balance", style = MaterialTheme.typography.bodyMedium)
        MoneyDisplay(
            amount = totalBalance,
            size = MoneySize.Large,
            animate = true
        )
    }
}
```

2. **Account Cards:**
```kotlin
accounts.forEach { account ->
    PyeraCard(
        onClick = { /* Navigate to detail */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                AccountIcon(account.type)
                Column {
                    Text(account.name, style = MaterialTheme.typography.titleMedium)
                    Text(account.type, style = MaterialTheme.typography.bodySmall)
                }
            }
            MoneyDisplay(amount = account.balance, size = MoneySize.Small)
        }
    }
    Spacer(modifier = Modifier.height(SpacingTokens.Medium))
}
```

### Screen 10: AccountDetailScreen.kt
Similar to AccountsScreen with transaction list.

### Screen 11: TransferScreen.kt
Form layout similar to AddTransactionScreen.

**Stop and verify:**
- [ ] All account screens compile
- [ ] Transfer screen works
- [ ] Money displays animate

---

## PHASE 7: Secondary Screens (6 screens)

### Screen 12: DebtScreen.kt
**File:** `app/src/main/java/com/pyera/app/ui/debt/DebtScreen.kt`

**Redesign Instructions:**

1. **Tab Row:**
```kotlin
TabRow(
    selectedTabIndex = selectedTab,
    containerColor = ColorTokens.SurfaceLevel1
) {
    Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("I Owe") }
    )
    Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Owed to Me") }
    )
}
```

2. **Debt Cards:**
```kotlin
debts.forEach { debt ->
    PyeraCard {
        Column(modifier = Modifier.padding(SpacingTokens.Medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(debt.personName, style = MaterialTheme.typography.titleMedium)
                MoneyDisplay(
                    amount = debt.amount,
                    isPositive = debt.type == "RECEIVABLE"
                )
            }
            
            // Due date, status, etc.
        }
    }
}
```

### Screens 13-17: Savings, Investments, Bills, Analysis, Insights
Apply same patterns:
- ✅ PyeraCard for content containers
- ✅ MoneyDisplay for amounts
- ✅ SpacingTokens for padding
- ✅ ColorTokens for colors
- ✅ Section headers
- ✅ List items

---

## PHASE 8: Settings & Security (5 screens)

### Screen 18: ProfileScreen.kt
**File:** `app/src/main/java/com/pyera/app/ui/profile/ProfileScreen.kt`

**Redesign Instructions:**

1. **Profile Header:**
```kotlin
PyeraCard(variant = CardVariant.Elevated) {
    Column(
        modifier = Modifier.padding(SpacingTokens.Large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(ColorTokens.Primary500, CircleShape)
        ) {
            Text(user.initials, color = Color.White, fontSize = 32.sp)
        }
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
        
        Text(user.name, style = MaterialTheme.typography.headlineSmall)
        Text(user.email, style = MaterialTheme.typography.bodyMedium)
    }
}
```

2. **Settings List:**
```kotlin
settingsItems.forEach { item ->
    PyeraCard(
        onClick = item.onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(item.icon, contentDescription = null)
            Spacer(modifier = Modifier.width(SpacingTokens.Medium))
            Text(item.title, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
    Spacer(modifier = Modifier.height(SpacingTokens.Small))
}
```

### Screen 19: ThemeSettingsScreen.kt
Use Material 3 ListItem with radio buttons.

### Screen 20: SecuritySettingsScreen.kt
Similar to ProfileScreen with security options.

### Screen 21: AppLockScreen.kt
**File:** `app/src/main/java/com/pyera/app/ui/security/AppLockScreen.kt`

**Redesign Instructions:**

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(ColorTokens.SurfaceLevel0)
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ColorTokens.Primary500
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Large))
        
        Text("Enter PIN", style = MaterialTheme.typography.headlineSmall)
        
        Spacer(modifier = Modifier.height(SpacingTokens.XXL))
        
        // PIN dots
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)) {
            repeat(4) { index ->
                PinDot(filled = index < enteredPin.length)
            }
        }
        
        Spacer(modifier = Modifier.height(SpacingTokens.XXL))
        
        // Keypad
        Keypad(onDigitClick = { /* */ })
    }
}
```

### Screen 22: SetPinScreen.kt
Similar to AppLockScreen with setup flow.

**Stop and verify:**
- [ ] All settings screens compile
- [ ] Security screens work
- [ ] Navigation between settings works

---

## FINAL VERIFICATION

### Build Verification
```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
```

### Quality Checklist
- [ ] All 22 screens compile without errors
- [ ] No deprecated color warnings
- [ ] App runs without crashes
- [ ] Dark mode looks correct
- [ ] Light mode looks correct
- [ ] All money displays animate
- [ ] All cards use PyeraCard
- [ ] All spacing uses SpacingTokens
- [ ] All colors use ColorTokens
- [ ] Accessibility labels present
- [ ] Touch targets minimum 48dp

### Visual QA
- [ ] Screenshots of all screens in dark mode
- [ ] Screenshots of all screens in light mode
- [ ] Compare with Figma designs

---

## MIGRATION TABLE REFERENCE

| Find | Replace With |
|------|--------------|
| `AccentGreen` | `ColorTokens.Primary500` |
| `NeonYellow` | `ColorTokens.Primary500` |
| `DarkGreen` | `ColorTokens.SurfaceLevel0` |
| `SurfaceDark` | `ColorTokens.SurfaceLevel1` |
| `SurfaceElevated` | `ColorTokens.SurfaceLevel2` |
| `ColorError` | `ColorTokens.Error500` |
| `ColorSuccess` | `ColorTokens.Success500` |
| `ColorWarning` | `ColorTokens.Warning500` |
| `16.dp` | `SpacingTokens.Medium` |
| `24.dp` | `SpacingTokens.Large` |
| `Card { }` | `PyeraCard { }` |
| `ModernCard { }` | `PyeraCard { }` |
| `Text("₱$amount")` | `MoneyDisplay(amount = amount)` |

---

## STOP CONDITIONS

**Stop immediately and report if:**
1. Build fails after any phase
2. App crashes on launch
3. Visual regression from current design
4. Accessibility features broken
5. Performance degradation (>100ms lag)

---

**Execute Phase 1 now. Confirm completion before proceeding to Phase 2.**
