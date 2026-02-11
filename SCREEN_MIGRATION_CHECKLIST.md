# Screen Migration Checklist

Use this checklist when migrating each screen.

## Pre-Migration
- [ ] Create git branch: `git checkout -b feature/redesign-<screen-name>`
- [ ] Screenshot current screen for comparison
- [ ] Review Figma design spec

## Migration Steps

### 1. Update Imports
```kotlin
// Remove old imports
import com.pyera.app.ui.theme.AccentGreen // ❌ REMOVE
import com.pyera.app.ui.theme.DarkGreen   // ❌ REMOVE

// Add new imports
import com.pyera.app.ui.theme.tokens.ColorTokens  // ✅ ADD
import com.pyera.app.ui.theme.tokens.SpacingTokens // ✅ ADD
import com.pyera.app.ui.components.PyeraCard       // ✅ ADD
import com.pyera.app.ui.components.MoneyDisplay    // ✅ ADD
```

### 2. Replace Colors
| Find | Replace |
|------|---------|
| `AccentGreen` | `ColorTokens.Primary500` |
| `NeonYellow` | `ColorTokens.Primary500` |
| `DarkGreen` | `ColorTokens.SurfaceLevel0` |
| `SurfaceDark` | `ColorTokens.SurfaceLevel1` |
| `SurfaceElevated` | `ColorTokens.SurfaceLevel2` |
| `ColorError` | `ColorTokens.Error500` |
| `ColorSuccess` | `ColorTokens.Success500` |
| `ColorWarning` | `ColorTokens.Warning500` |

### 3. Replace Spacing
| Find | Replace |
|------|---------|
| `4.dp` | `SpacingTokens.ExtraSmall` |
| `8.dp` | `SpacingTokens.Small` |
| `12.dp` | `SpacingTokens.MediumSmall` |
| `16.dp` | `SpacingTokens.Medium` |
| `20.dp` | `SpacingTokens.MediumLarge` |
| `24.dp` | `SpacingTokens.Large` |
| `32.dp` | `SpacingTokens.ExtraLarge` |

### 4. Replace Components
| Find | Replace |
|------|---------|
| `Card { }` | `PyeraCard { }` |
| `ModernCard { }` | `PyeraCard { }` |
| `PyeraCard { }` | `PyeraCard(variant = CardVariant.Default) { }` |
| `Text("₱$amount")` | `MoneyDisplay(amount = amount)` |

### 5. Update Icons
```kotlin
// Find all icon imports
import androidx.compose.material.icons.Icons

// Replace with Phosphor Icons
import com.phosphor.phosphoricons.PhosphorIcons
```

## Post-Migration Checklist

- [ ] Screen compiles without errors
- [ ] No deprecated color warnings
- [ ] Visual matches Figma design
- [ ] Dark mode works correctly
- [ ] Light mode works correctly
- [ ] All interactions work
- [ ] Accessibility labels present
- [ ] Screenshot new design
- [ ] Commit changes
- [ ] Create PR

## Screen-Specific Notes

### LoginScreen.kt
- Focus: Clean form, gradient background
- Special: Edge-to-edge, IME padding

### DashboardScreen.kt
- Focus: Hero balance, quick actions
- Special: Animated MoneyDisplay, swipeable transactions

### TransactionListScreen.kt
- Focus: Sticky headers, swipe actions
- Special: Pull-to-refresh, empty state

### BudgetScreen.kt
- Focus: Progress bars, status indicators
- Special: Color-coded progress (green/yellow/red)

### AddTransactionScreen.kt
- Focus: Clean form, validation
- Special: Number keyboard for amount

## Common Issues & Fixes

### Issue: "ColorTokens not found"
**Fix:** Add import
```kotlin
import com.pyera.app.ui.theme.tokens.ColorTokens
```

### Issue: "Unresolved reference: SpacingTokens"
**Fix:** Add import
```kotlin
import com.pyera.app.ui.theme.tokens.SpacingTokens
```

### Issue: Cards look wrong
**Fix:** Use CardVariant
```kotlin
PyeraCard(variant = CardVariant.Default) // or Elevated, Outlined
```

### Issue: Money not animating
**Fix:** Add animate parameter
```kotlin
MoneyDisplay(amount = balance, animate = true)
```

## PR Template

```markdown
## Screen Redesign: <ScreenName>

### Changes
- Updated to new design system
- Migrated colors to ColorTokens
- Migrated spacing to SpacingTokens
- Replaced Card with PyeraCard
- Added MoneyDisplay for amounts

### Screenshots
| Before | After |
|--------|-------|
| ![Before](before.png) | ![After](after.png) |

### Testing
- [ ] Dark mode
- [ ] Light mode
- [ ] Accessibility
- [ ] All interactions

### Related
Closes #<issue-number>
```
