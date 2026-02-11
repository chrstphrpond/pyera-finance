# GPT Codex - Quick Execution Prompt

**Give this to GPT Codex to start immediately:**

---

## PROMPT

You are implementing a UI redesign for Pyera Finance Android app. 

**Location:** `c:\coding\Pyera`

### Already Implemented (Don't Modify)
- ✅ ColorTokens.kt (60+ colors)
- ✅ SpacingTokens.kt
- ✅ PyeraCard component
- ✅ MoneyDisplay component

### Your Task
Update ALL 22 screens to use the new design system:

**Step 1: Replace Colors**
```
AccentGreen → ColorTokens.Primary500
DarkGreen → ColorTokens.SurfaceLevel0
SurfaceDark → ColorTokens.SurfaceLevel1
SurfaceElevated → ColorTokens.SurfaceLevel2
ColorError → ColorTokens.Error500
ColorSuccess → ColorTokens.Success500
```

**Step 2: Replace Spacing**
```
16.dp → SpacingTokens.Medium
24.dp → SpacingTokens.Large
32.dp → SpacingTokens.ExtraLarge
```

**Step 3: Replace Components**
```
Card { } → PyeraCard { }
ModernCard { } → PyeraCard { }
Text("₱$amount") → MoneyDisplay(amount = amount)
```

### Screen Priority Order
1. LoginScreen.kt
2. DashboardScreen.kt
3. TransactionListScreen.kt
4. BudgetScreen.kt
5. AccountsScreen.kt
6. All remaining screens

### Verification After Each Screen
```bash
./gradlew :app:compileDebugKotlin
```

### Stop If
- Build fails
- App crashes
- Any screen looks broken

**Start with LoginScreen.kt now.**

---

## For Detailed Implementation

See full prompt: `CODEX_EXECUTE_REDESIGN.md` (23KB)
