# Pyera Finance Redesign - Quick Reference

## Before vs After Comparison

| Element | Before (Nocturne Ember) | After (Midnight Slate) |
|---------|------------------------|------------------------|
| **Background** | Dark green `#0A0E0D` | Deep slate `#020617` |
| **Primary** | Neon yellow `#D4FF00` | Trust blue `#3D5AFE` |
| **Secondary** | Accent green `#9FD356` | Growth green `#00C853` |
| **Cards** | 3 inconsistent variants | Unified with elevation levels |
| **Typography** | Ibrand + Outfit (2 fonts) | Inter variable (1 font) |
| **Icons** | Mixed Material + custom | Phosphor Icons (consistent) |
| **Corner Radius** | Mixed 8dp, 12dp, 16dp | Systematic: 8, 12, 16, 24 |

---

## Color Palette Reference

### Primary Colors
```kotlin
Primary500 = Color(0xFF3D5AFE)  // Main brand color - Trust Blue
Primary600 = Color(0xFF304FFE)  // Hover/pressed state
Primary900 = Color(0xFF1A237E)  // Dark variant for surfaces
```

### Surface Elevations (Dark Mode)
```kotlin
SurfaceLevel0 = Color(0xFF020617)  // Deepest background
SurfaceLevel1 = Color(0xFF0F172A)  // Cards
SurfaceLevel2 = Color(0xFF1E293B)  // Elevated cards, dialogs
SurfaceLevel3 = Color(0xFF334155)  // Menus, popovers
```

### Semantic Colors
```kotlin
Success500 = Color(0xFF00C853)  // Income, positive
Error500   = Color(0xFFFF5252)  // Expense, negative, alerts
Warning500 = Color(0xFFFFB300)  // Warnings, near limits
Info500    = Color(0xFF448AFF)  // Info, actions
```

---

## Typography Scale

| Style | Size | Weight | Usage |
|-------|------|--------|-------|
| Display Large | 57sp | Light (300) | Hero balances |
| Display Medium | 45sp | Normal (400) | Screen titles |
| Headline Large | 32sp | Medium (500) | Section headers |
| Title Large | 22sp | Medium (500) | Card titles |
| Title Medium | 16sp | Medium (500) | List item titles |
| Body Large | 16sp | Normal (400) | Primary text |
| Body Medium | 14sp | Normal (400) | Secondary text |
| Label Large | 14sp | Medium (500) | Buttons, chips |

### Money Display Typography
```kotlin
// Large: ₱12,345.67 (for hero sections)
MoneyLarge = 40sp, SemiBold, tabular numbers

// Medium: ₱12,345.67 (for list items)
MoneyMedium = 28sp, SemiBold, tabular numbers

// Small: ₱12,345.67 (for compact displays)
MoneySmall = 16sp, SemiBold, tabular numbers
```

---

## Spacing System

| Token | Value | Usage |
|-------|-------|-------|
| `ExtraSmall` | 4dp | Icon padding, tight gaps |
| `Small` | 8dp | Related element gaps |
| `Medium` | 16dp | Standard padding, card gutters |
| `Large` | 24dp | Section separators |
| `ExtraLarge` | 32dp | Major section breaks |
| `XXL` | 48dp | Screen edge padding |

**Rule of thumb:** Use multiples of 8dp for all spacing.

---

## Component Patterns

### Card Variants

```kotlin
// Default - Most common
PyeraCard(variant = CardVariant.Default) {
    // Content with 16dp internal padding
}
// Use for: Transaction items, account cards, settings

// Elevated - Attention-grabbing
PyeraCard(variant = CardVariant.Elevated) {
    // Hero balance, featured content
}
// Use for: Dashboard hero, featured budgets

// Outlined - Subtle
PyeraCard(variant = CardVariant.Outlined) {
    // Secondary content
}
// Use for: Disabled states, placeholder content
```

### Button Hierarchy

```kotlin
// Primary - Main actions
Button(onClick = { }) { Text("Sign In") }
// Use for: Submit, Save, Continue

// Secondary - Alternative actions
OutlinedButton(onClick = { }) { Text("Cancel") }
// Use for: Cancel, Back, Dismiss

// Tertiary - Low emphasis
TextButton(onClick = { }) { Text("Skip") }
// Use for: Skip, Learn more, Forgot password

// FAB - Primary creation action
FloatingActionButton(onClick = { }) {
    Icon(PyeraIcons.Add, null)
}
// Use for: Add transaction, Create budget
```

### Money Display

```kotlin
// Income (green)
MoneyDisplay(
    amount = 1500.00,
    isPositive = true  // Green color
)

// Expense (red)
MoneyDisplay(
    amount = 85.50,
    isPositive = false  // Red color
)

// Neutral (default text color)
MoneyDisplay(
    amount = 5000.00,
    isPositive = null  // Gray/default color
)

// Large hero display
MoneyDisplay(
    amount = totalBalance,
    size = MoneySize.Large,
    animate = true  // Count-up animation
)
```

---

## Screen-Specific Guidelines

### Dashboard
- **Hero card:** Elevated variant, large money display, income/expense summary
- **Quick actions:** Horizontal scroll, icon + label, 4 items max
- **Recent transactions:** 5 items max, "See All" link
- **Bottom padding:** 80dp (for FAB)

### Transaction List
- **Sticky headers:** Date grouping with surface background
- **Swipe actions:** Left = Edit (blue), Right = Delete (red)
- **Empty state:** Illustration + primary action button
- **Pull-to-refresh:** Shimmer skeleton while loading

### Budget
- **Progress bars:** 8dp height, rounded caps, color-coded
- **Over budget:** Red text + warning message below
- **Near limit (>80%):** Yellow warning color
- **Card order:** Active first, then paused, then completed

### Authentication
- **Background:** Subtle radial gradient from primary
- **Logo:** Centered, 80dp size
- **Form:** Full-width inputs, 16dp vertical spacing
- **Keyboard:** Actions handle next/done navigation

---

## Animation Guidelines

### Duration Standards
| Animation Type | Duration | Easing |
|----------------|----------|--------|
| Micro (button press) | 100ms | Linear |
| Standard (transitions) | 300ms | FastOutSlowIn |
| Emphasis (money display) | 800ms | FastOutSlowIn |
| Spring (bounce effects) | 500ms | Spring (bouncy) |

### Common Animations
```kotlin
// Page navigation
enter = slideInHorizontally { it } + fadeIn()
exit = slideOutHorizontally { -it/3 } + fadeOut()

// Content appearance
AnimatedVisibility(visible = isVisible) {
    // Content fades and slides in
}

// Money counting
animateFloatAsState(
    targetValue = amount,
    animationSpec = tween(800, easing = FastOutSlowIn)
)

// Success check
scaleIn(spring(dampingRatio = Bouncy, stiffness = Low))
```

---

## Iconography

### Icon Sizes
| Size | Dimensions | Usage |
|------|------------|-------|
| Small | 16dp | Inline with text, compact UI |
| Medium | 24dp | Standard buttons, list items |
| Large | 32dp | Feature highlights, empty states |
| Extra Large | 48dp | Navigation, primary actions |

### Icon States
- **Default:** Outlined variant (Phosphor Regular)
- **Active/Selected:** Filled variant (Phosphor Fill)
- **Disabled:** 38% opacity

### Common Icons
```kotlin
// Navigation
Home / HomeFilled
ChartPieSlice / ChartPieSliceFilled  // Budget
User / UserFilled  // Profile

// Actions
Plus  // Add
MagnifyingGlass  // Search
Funnel  // Filter
DotsThreeVertical  // More options

// Transactions
ArrowUp  // Income (green)
ArrowDown  // Expense (red)
ArrowRight  // Transfer

// Categories (examples)
Hamburger  // Food
Car  // Transport
ShoppingBag  // Shopping
Lightning  // Utilities
Heart  // Health
GraduationCap  // Education
```

---

## Accessibility Checklist

### Visual
- [ ] Color contrast ratio ≥ 4.5:1 for all text
- [ ] Touch targets minimum 48x48dp
- [ ] Focus indicators visible on all interactive elements
- [ ] Text scales properly with system font size

### Screen Reader (TalkBack)
- [ ] All icons have content descriptions
- [ ] Images have descriptive alt text
- [ ] Decorative images marked as null description
- [ ] Custom actions announced properly

### Interaction
- [ ] Keyboard navigation works throughout app
- [ ] Form error messages announced
- [ ] Loading states communicated
- [ ] Success/failure actions have haptic feedback

---

## Migration Checklist (Existing Code)

### Colors
```kotlin
// OLD → NEW
AccentGreen / NeonYellow → Primary500
DarkGreen → SurfaceLevel0
SurfaceDark → SurfaceLevel1
SurfaceElevated → SurfaceLevel2
ColorError → Error500
ColorSuccess → Success500
```

### Components
```kotlin
// OLD → NEW
ModernCard / PyeraCard / Card → PyeraCard(variant = ...)
PyeraButton → Button (Material 3)
BalanceDisplayLarge → MoneyDisplay(size = MoneySize.Large)
```

### Typography
```kotlin
// OLD → NEW
Text(fontSize = 24.sp, fontWeight = Bold) → 
    Text(style = MaterialTheme.typography.headlineSmall)
```

---

## Common Issues & Solutions

### Issue: Text doesn't align in money displays
**Solution:** Use `fontFeatureSettings = "tnum"` for tabular numbers

### Issue: Cards look inconsistent
**Solution:** Always use `PyeraCard` with appropriate variant, never raw `Card`

### Issue: Animations feel sluggish
**Solution:** Check for heavy operations during animation. Use `derivedStateOf` for calculations.

### Issue: Dark mode looks wrong
**Solution:** Ensure using `SurfaceLevelX` colors, not hardcoded hex values

### Issue: Icons look different sizes
**Solution:** Always use `IconSize` enum, never hardcoded sizes

---

## Resources

### Figma File (Create)
- Design system tokens
- All screen mockups
- Component library
- Icon set

### Documentation
- `REDESIGN_IMPLEMENTATION_PLAN.md` - Full implementation guide
- `docs/DESIGN_SYSTEM.md` - Component documentation
- `docs/CODE_STYLE.md` - Coding standards

### Tools
- Phosphor Icons: https://phosphoricons.com
- Material 3: https://m3.material.io
- Inter Font: https://rsms.me/inter

---

**Questions? Refer to the full implementation plan or ask the design team.**
