# Pyera Finance - Figma Design Specification

> **For Designers:** Use this as a reference when creating Figma components and mockups.

---

## Brand Identity

### Brand Promise
"Pyera helps you understand and control your finances with clarity and confidence."

### Brand Attributes
- **Trustworthy:** Secure, reliable, professional
- **Modern:** Cutting-edge, clean, sophisticated
- **Approachable:** Friendly, not intimidating, clear
- **Empowering:** Gives users control and insight

### Voice & Tone
- Clear and direct, no jargon
- Encouraging but not overly casual
- Professional but not cold
- Action-oriented

---

## Color System

### Primary Palette
```
Primary 50:   #EEF4FF   (Light backgrounds, hover states)
Primary 100:  #D9E6FF   (Light surfaces)
Primary 200:  #B3D1FF   (Borders, dividers)
Primary 500:  #3D5AFE   ⭐ BRAND COLOR
Primary 600:  #304FFE   (Pressed states)
Primary 700:  #283593   (Text on light)
Primary 900:  #1A237E   (Dark surfaces)
```

### Neutral Palette (Midnight Slate)
```
Slate 50:   #F8FAFC   (Light mode backgrounds)
Slate 100:  #F1F5F9   (Light mode surfaces)
Slate 200:  #E2E8F0   (Borders light)
Slate 400:  #94A3B8   (Secondary text light)
Slate 600:  #475569   (Secondary text dark)
Slate 700:  #334155   (Borders dark)
Slate 800:  #1E293B   (Elevated dark surfaces)
Slate 900:  #0F172A   ⭐ DARK MODE CARD BACKGROUND
Slate 950:  #020617   ⭐ DARK MODE APP BACKGROUND
```

### Semantic Colors
```
Success:  #00C853   (Income, positive, available)
Error:    #FF5252   (Expense, negative, alerts)
Warning:  #FFB300   (Caution, near limits)
Info:     #448AFF   (Actions, links, info)
```

### Usage Matrix

| Element | Light Mode | Dark Mode |
|---------|-----------|-----------|
| App Background | Slate 50 | Slate 950 |
| Card Background | White | Slate 900 |
| Elevated Card | Slate 100 | Slate 800 |
| Primary Text | Slate 900 | Slate 50 |
| Secondary Text | Slate 600 | Slate 400 |
| Borders | Slate 200 | Slate 700 |
| Primary Button | Primary 500 | Primary 500 |
| Success/Positive | Success | Success |
| Error/Negative | Error | Error |

---

## Typography

### Font Family
**Primary:** Inter (Google Fonts)
- Variable font: Weight 100-900
- Download: https://rsms.me/inter

### Type Scale

| Style | Size | Line Height | Weight | Letter Spacing | Usage |
|-------|------|-------------|--------|----------------|-------|
| **Display Large** | 57px | 64px | 300 | -0.25px | Hero balance |
| **Display Medium** | 45px | 52px | 400 | 0 | Large amounts |
| **Display Small** | 36px | 44px | 400 | 0 | Screen titles |
| **Headline Large** | 32px | 40px | 500 | 0 | Section headers |
| **Headline Medium** | 28px | 36px | 500 | 0 | Card titles |
| **Headline Small** | 24px | 32px | 500 | 0 | Dialog titles |
| **Title Large** | 22px | 28px | 500 | 0 | List headers |
| **Title Medium** | 16px | 24px | 500 | 0.15px | List items |
| **Title Small** | 14px | 20px | 500 | 0.1px | Labels |
| **Body Large** | 16px | 24px | 400 | 0.5px | Primary text |
| **Body Medium** | 14px | 20px | 400 | 0.25px | Secondary text |
| **Body Small** | 12px | 16px | 400 | 0.4px | Captions |
| **Label Large** | 14px | 20px | 500 | 0.1px | Buttons |
| **Label Medium** | 12px | 16px | 500 | 0.5px | Small buttons |
| **Label Small** | 11px | 16px | 500 | 0.5px | Tags |

### Money Display (Special)
```
Money Large:   40px / 48px / 600 (Semibold) / Tabular
Money Medium:  28px / 36px / 600 (Semibold) / Tabular
Money Small:   16px / 24px / 600 (Semibold) / Tabular
```
**Note:** All money displays use tabular figures (fixed-width numbers) for alignment.

### Typography Do's and Don'ts

✅ **DO:**
- Use the full type scale consistently
- Use tabular figures for money
- Ensure sufficient line height for readability
- Use weight to create hierarchy, not just size

❌ **DON'T:**
- Use font sizes outside the scale
- Use italic for emphasis (use weight instead)
- Use ALL CAPS for body text
- Go below 11px for any text

---

## Spacing System

### Base Unit: 4px
All spacing is a multiple of 4px.

### Spacing Tokens
```
0px   - None (0×)
4px   - Extra Small (1×)
8px   - Small (2×)
12px  - Medium Small (3×)
16px  - Medium (4×) ⭐ MOST COMMON
20px  - Medium Large (5×)
24px  - Large (6×)
32px  - Extra Large (8×)
48px  - XXL (12×)
64px  - XXXL (16×)
```

### Common Patterns
```
// Card internal padding
Card padding: 16px

// Screen edge padding  
Screen horizontal: 16px
Screen top: 24px (below status bar)
Screen bottom: 80px (above nav bar + FAB)

// Section spacing
Between sections: 32px
Between cards: 16px
Between list items: 8px
Within card items: 16px
```

### Layout Grids

**Mobile (360dp - 412dp width):**
- 4-column grid
- 16px margins
- 16px gutters

**Tablet (600dp+ width):**
- 8-column grid
- 24px margins
- 16px gutters

---

## Elevation & Shadows

### Surface Elevation (Dark Mode)
```
Level 0: #020617  (Base background)
Level 1: #0F172A  (Cards) - +1dp elevation
Level 2: #1E293B  (Elevated cards, dialogs) - +3dp elevation
Level 3: #334155  (Menus, popovers) - +6dp elevation
```

### Card Elevation
```
Default Card:   1dp elevation, 1px border (Slate 800)
Elevated Card:  3dp elevation, no border
Outlined Card:  0dp elevation, 1px border (Slate 700)
```

### Shadow Values (for reference)
```
Shadow 1: 0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)
Shadow 2: 0 3px 6px rgba(0,0,0,0.15), 0 2px 4px rgba(0,0,0,0.12)
Shadow 3: 0 10px 20px rgba(0,0,0,0.15), 0 3px 6px rgba(0,0,0,0.10)
```

---

## Component Library

### Buttons

**Primary Button**
- Height: 48px
- Padding: 0 24px
- Border radius: 12px
- Background: Primary 500
- Text: White, Label Large (14px, 500)
- Pressed: Primary 600
- Disabled: 38% opacity

**Secondary Button (Outlined)**
- Same dimensions as primary
- Background: Transparent
- Border: 1px Slate 700 (dark) / Slate 200 (light)
- Text: Primary 500

**Text Button**
- Height: 40px
- Padding: 0 12px
- Text: Primary 500
- Pressed: 12% Primary 500 overlay

**Floating Action Button (FAB)**
- Size: 56dp × 56dp
- Border radius: 16dp
- Background: Primary 500
- Icon: White, 24dp
- Shadow: 3dp

### Cards

**Default Card**
- Padding: 16px
- Border radius: 12px
- Background: Slate 900 (dark)
- Border: 1px Slate 800
- Elevation: 1dp

**Elevated Card**
- Same padding and radius
- Background: Slate 800
- No border
- Elevation: 3dp

**Clickable Card**
- Ripple effect: 12% Primary 500
- Pressed state: -1dp elevation

### Inputs

**Outlined Text Field**
- Height: 56px
- Border radius: 12px
- Border: 1px Slate 700 (resting)
- Border focused: 2px Primary 500
- Label: Body Medium (14px)
- Text: Body Large (16px)

### Lists

**List Item (Single Line)**
- Height: 56px
- Padding: 16px horizontal
- Icon: 24dp, left-aligned
- Text: Title Medium (16px, 500)

**List Item (Two Line)**
- Height: 72px
- Padding: 16px horizontal
- Primary text: Title Medium (16px)
- Secondary text: Body Medium (14px)

**List Item (with Icon)**
- Icon size: 24dp
- Icon color: Slate 400 (inactive), Primary 500 (active)
- Icon-text spacing: 16px

---

## Icons

### Icon System: Phosphor Icons
- Style: Consistent 2px stroke weight
- Variants: Regular, Fill, Bold, Duotone
- Corner radius: Rounded (not sharp)

### Icon Sizes
```
Small:   16dp  (Inline with text)
Medium:  24dp  (Buttons, list items) ⭐ MOST COMMON
Large:   32dp  (Feature highlights)
XLarge:  48dp  (Navigation, empty states)
```

### Icon Colors
```
Default:       Slate 400 (inactive), Slate 100 (active)
Primary:       Primary 500
Success:       Success 500
Error:         Error 500
Warning:       Warning 500
Disabled:      38% opacity of default
```

### Key Icons by Category

**Navigation**
- Home
- ChartPieSlice (Budget)
- User (Profile)

**Actions**
- Plus (Add)
- MagnifyingGlass (Search)
- Funnel (Filter)
- DotsThreeVertical (More)
- ArrowRight

**Transactions**
- ArrowUp (Income)
- ArrowDown (Expense)
- ArrowsLeftRight (Transfer)
- Scanner (Receipt scan)

**Status**
- CheckCircle (Success)
- WarningCircle (Warning)
- Info (Info)
- XCircle (Error)

---

## Screen Specifications

### Dashboard

**Structure (Top to Bottom):**
1. Status bar (system)
2. App bar (optional, transparent)
3. **Hero Balance Card** - Elevated, full width
4. **Quick Actions Row** - Horizontal scroll
5. **Recent Transactions** - Section header + 5 items
6. **Active Budgets** - Carousel or list
7. Bottom nav bar (system)

**Hero Card Specs:**
- Variant: Elevated
- Padding: 24px
- Balance: Display Large (or Money Large)
- Income/Expense row: Two columns
- Icons: 24dp, colored

**Quick Actions:**
- 4 items in horizontal scroll
- Icon size: 32dp
- Label: Label Medium
- Spacing: 16px between items

### Transaction List

**Structure:**
1. App bar with title + filter/search icons
2. Filter chips (horizontal scroll, optional)
3. **Date headers** - Sticky, surface color
4. **Transaction items** - Default cards, swipeable
5. FAB (bottom right)

**Transaction Item:**
- Default card variant
- Left: Category icon (40dp circle background)
- Middle: Note + Date
- Right: Amount (Money Small)
- Swipe: Edit (left), Delete (right)

### Budget

**Structure:**
1. App bar
2. **Summary card** (if multiple budgets)
3. **Budget list** - Full width cards

**Budget Card:**
- Default variant
- Top row: Category name + Remaining amount
- Progress bar: 8dp height, rounded caps
- Bottom row: % used + Spent/Total
- Over budget: Red text + warning

### Authentication

**Login/Register:**
- Full-screen gradient background
- Centered card or form
- Logo at top (80dp)
- Form inputs: Full width, 16px vertical spacing
- Primary button: Full width
- Alternative auth below (Google)
- Bottom link for switch (Sign up/Sign in)

---

## Animations & Interactions

### Micro-interactions

**Button Press:**
- Scale: 100% → 97%
- Duration: 100ms
- Easing: Linear

**Card Press:**
- Elevation: Current → Current - 1dp
- Background: +4% white overlay
- Duration: 100ms

**Money Display:**
- Count up from 0
- Duration: 800ms
- Easing: FastOutSlowIn

**Success Animation:**
- Checkmark scales in with bounce
- Duration: 500ms
- Easing: Spring (damping 0.6)

### Page Transitions

**Push Navigation:**
- Enter: Slide from right + fade in
- Exit: Slide to left (33%) + fade out
- Duration: 300ms

**Modal (Bottom Sheet):**
- Enter: Slide up from bottom
- Duration: 300ms
- Background: Fade in black 50%

**Dialog:**
- Enter: Scale from 90% + fade in
- Duration: 200ms

### Loading States

**Skeleton Loading:**
- Shimmer animation left to right
- Duration: 1.5s
- Background: Slate 800
- Shimmer: Slate 700

**Pull to Refresh:**
- Circular progress indicator
- Color: Primary 500
- Trigger threshold: 80dp

---

## Responsive Behavior

### Breakpoints
```
Compact:  0 - 599dp   (Phone portrait)
Medium:   600 - 839dp (Phone landscape, tablet portrait)
Expanded: 840dp+      (Tablet landscape)
```

### Layout Changes

**Compact:**
- Single column
- Bottom navigation
- Full-width cards

**Medium:**
- Two-column grid for cards
- Side navigation (optional)
- Max content width: 600dp

**Expanded:**
- Three-column grid
- Rail navigation
- Max content width: 840dp

---

## Accessibility Requirements

### Color Contrast (WCAG 2.1 Level AA)
```
Normal text (<18px):     4.5:1 minimum
Large text (≥18px bold): 3:1 minimum
UI components:           3:1 minimum
```

### Touch Targets
```
Minimum: 48dp × 48dp
Recommended: 56dp × 56dp for primary actions
Spacing between: 8dp minimum
```

### Focus Indicators
- Width: 2dp
- Color: Primary 500
- Offset: 2dp from element
- Border radius: Match element + 2dp

### Screen Reader
- All icons: Content description
- Images: Descriptive alt text
- Decorative: Empty description
- Status: Live regions for updates

---

## Asset Export Checklist

### For Developers

**Icons:**
- [ ] All icons exported as Vector Drawable (.xml)
- [ ] Naming: `ic_<name>_<variant>` (e.g., `ic_home_fill`)
- [ ] Consistent 24dp viewport

**Images:**
- [ ] Empty state illustrations: 200dp × 200dp
- [ ] Onboarding illustrations: Full width, 16:9 ratio
- [ ] Format: WebP with transparency

**Colors:**
- [ ] All colors in design system documented
- [ ] Dark mode variants specified
- [ ] No one-off hex codes

**Fonts:**
- [ ] Inter variable font (TTF)
- [ ] License included

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | Feb 2026 | Initial redesign spec |

---

**Questions? Contact:** design-team@pyera.app
