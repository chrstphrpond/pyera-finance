# 🚀 START HERE - Codex Implementation Guide

## Quick Start (For GPT Codex)

### Step 1: Read Complete Prompt
Open and read the entire file: **`CODEX_COMPLETE_PROMPT.md`**

This contains:
- ✅ All token files with complete code
- ✅ Component implementations
- ✅ Step-by-step instructions
- ✅ Verification checklist

### Step 2: Execute in Order

```
SECTION 1: Foundation (Tokens & Theme)
  ↓
SECTION 2: Components (Cards, MoneyDisplay)
  ↓
SECTION 3: Screen Migration
  ↓
SECTION 4: Verification
```

### Step 3: Verify Each Step

After each section, run:
```bash
./gradlew :app:compileDebugKotlin
```

**If build fails, fix errors before continuing.**

---

## File Priority Order

### Highest Priority (Create These First)

| Priority | File | Purpose |
|----------|------|---------|
| 1 | `ColorTokens.kt` | All colors for the app |
| 2 | `SpacingTokens.kt` | Consistent spacing |
| 3 | `Theme.kt` | Apply new color scheme |
| 4 | `Type.kt` | Typography with money styles |
| 5 | `PyeraCard.kt` | Unified card component |
| 6 | `MoneyDisplay.kt` | Animated money display |

### Medium Priority (Update Existing)

| File | Changes |
|------|---------|
| `DashboardScreen.kt` | Use new colors & components |
| `TransactionListItem.kt` | Update to new design |
| `BudgetScreen.kt` | New progress bars |
| All other screens | Replace color imports |

---

## Key Requirements

### Must Use:
- ✅ `ColorTokens.Primary500` instead of `AccentGreen`
- ✅ `ColorTokens.SurfaceLevel1` instead of `SurfaceDark`
- ✅ `PyeraCard()` instead of `Card()` or `ModernCard()`
- ✅ `MoneyDisplay()` for all monetary amounts
- ✅ `SpacingTokens.Medium` (16.dp) instead of hardcoded values

### Must NOT Use:
- ❌ Hardcoded hex colors (#FF0000)
- ❌ Old color names (`AccentGreen`, `NeonYellow`)
- ❌ Mixed spacing values (8.dp, 14.dp, 18.dp)
- ❌ Inconsistent card styles

---

## Testing Checklist

After implementation, verify:

- [ ] App compiles without errors
- [ ] No deprecated color warnings
- [ ] Dark mode looks correct
- [ ] Light mode looks correct
- [ ] Money displays animate smoothly
- [ ] Cards have consistent styling
- [ ] All buttons use correct colors
- [ ] Text is readable (contrast ratio > 4.5:1)

---

## Color Mapping Reference

| Old (Remove) | New (Use) | Example |
|--------------|-----------|---------|
| `AccentGreen` | `ColorTokens.Primary500` | Buttons, active states |
| `NeonYellow` | `ColorTokens.Primary500` | Highlights, FAB |
| `DarkGreen` | `ColorTokens.SurfaceLevel0` | App background |
| `SurfaceDark` | `ColorTokens.SurfaceLevel1` | Card backgrounds |
| `SurfaceElevated` | `ColorTokens.SurfaceLevel2` | Elevated cards |
| `ColorError` | `ColorTokens.Error500` | Errors, expenses |
| `ColorSuccess` | `ColorTokens.Success500` | Success, income |

---

## Example: Before & After

### Before (Old Code)
```kotlin
Card(
    backgroundColor = SurfaceDark,
    elevation = 4.dp
) {
    Text(
        text = "₱1,234.56",
        color = AccentGreen,
        fontSize = 24.sp
    )
}
```

### After (New Code)
```kotlin
PyeraCard(
    variant = CardVariant.Default
) {
    MoneyDisplay(
        amount = 1234.56,
        size = MoneySize.Medium,
        isPositive = true
    )
}
```

---

## Troubleshooting

### Build Error: "Unresolved reference: ColorTokens"
**Fix:** Add import:
```kotlin
import com.pyera.app.ui.theme.tokens.ColorTokens
```

### Visual Issue: "Colors don't match design"
**Fix:** Check you're using tokens, not hardcoded values

### Performance Issue: "UI lags"
**Fix:** Ensure no heavy calculations in composables

---

## Final Deliverables

After completing all sections, the app should have:

1. ✅ Complete token system (4 files)
2. ✅ Updated theme with new colors
3. ✅ PyeraCard component (3 variants)
4. ✅ MoneyDisplay component (3 sizes + animation)
5. ✅ All screens using new design system
6. ✅ No deprecated color references
7. ✅ Consistent spacing throughout

---

## Questions?

Refer to:
- `REDESIGN_IMPLEMENTATION_PLAN.md` - Full strategy
- `REDESIGN_QUICK_REFERENCE.md` - Developer reference
- `REDESIGN_FIGMA_SPEC.md` - Design specifications

---

**Ready to start? Open `CODEX_COMPLETE_PROMPT.md` and begin with Section 1.**
