# Pyera Finance UI/UX Design Trends Report 2024-2025

## Executive Summary

The fintech app landscape in 2024-2025 is defined by **minimalist interfaces, bold brand differentiation, and mobile-first experiences** that prioritize trust and reduce cognitive load. Leading apps like Revolut, N26, Monzo, Robinhood, and Coinbase are moving away from traditional corporate aesthetics toward vibrant, personalized interfaces that feel more like social media than banking.

**Key Statistics:**
- 88% of mobile app users abandon an app after just two uses due to poor usability
- 73% of online adults expect to accomplish any financial task through a mobile app
- The European Accessibility Act (EAA) came into force in June 2025, requiring accessibility compliance

---

## 1. Color Trends

### 1.1 Modern Finance App Color Palettes (Dark Mode Focused)

**The Decline of Corporate Blue**
The fintech industry is experiencing a significant shift away from traditional navy, teal, and corporate blues. Instead, brands are using **color as a competitive advantage** through unexpected, distinctive choices.

**Leading Brand Examples:**
| App | Primary Color | Secondary/Accent | Character |
|-----|---------------|------------------|-----------|
| **Monzo** | Hot Coral (#FF4D4D) | Dark Navy | Disruptive, youthful |
| **Revolut** | Blue/White Gradient | Neon accents | Premium, global |
| **N26** | Transparent/White | Teal accents | Minimalist, European |
| **Robinhood** | Green (#00C805) | Black | Growth-focused |
| **Coinbase** | Blue (#0052FF) | White | Trustworthy, crypto-native |

**Recommended Dark Mode Palettes:**

```
Primary Dark Surface:    #0D0D0D to #121212 (Pure Dark)
Elevated Surfaces:       #1C1C1E to #2C2C2E (Card backgrounds)
Primary Text:            #FFFFFF (100% opacity)
Secondary Text:          #EBEBF5 (60% opacity)
Tertiary Text:           #EBEBF5 (38% opacity)
Accent Colors:
  - Success/Growth:      #34C759 (Apple-style green)
  - Warning/Alert:       #FF9500 (Amber)
  - Error/Loss:          #FF3B30 (Coral red)
  - Info/Primary:        #007AFF (iOS blue)
```

### 1.2 Gradient Usage Trends

**2025 Gradient Direction:**
- **Subtle overlays** rather than bold rainbow gradients
- **Glassmorphic gradients** using transparency and blur
- **Brand-colored gradients** for premium/Pro tiers
- **Dark-to-darker gradients** for depth in dark mode

**Popular Gradient Combinations:**
```
Premium/Gold:    linear-gradient(135deg, #1a1a1a 0%, #2d2420 100%)
Crypto/Neon:     linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%)
Natural/Green:   linear-gradient(135deg, #134e5e 0%, #71b280 100%)
Sunset/Orange:   linear-gradient(135deg, #2c1a1d 0%, #4a2c2a 100%)
```

### 1.3 Accent Color Strategies

**Functional Color Coding:**
- **Green shades**: Positive trends, growth, deposits, success states
- **Red/Orange**: Alerts, warnings, spending, negative trends
- **Gold/Navy**: Premium positioning, trust, stability (dominant 2025 combination)
- **Purple/Teal**: Crypto features, futuristic elements

**Best Practices:**
- Use maximum 5-6 colors in the palette
- Maintain minimum 4.5:1 contrast ratio (WCAG AA compliance)
- Test for color blindness compatibility
- Use color strategically to guide attention to critical information

### 1.4 Surface Elevation Colors

**Material 3 Elevation System for Dark Mode:**
| Elevation Level | Surface Color | Use Case |
|-----------------|---------------|----------|
| Level 0 | #121212 | Background |
| Level 1 | #1E1E1E | Cards (resting) |
| Level 2 | #232323 | Cards (pressed) |
| Level 3 | #252525 | Navigation, modals |
| Level 4 | #272727 | Floating elements |
| Level 5 | #2C2C2C | Top app bar, sheets |

**Visual Elevation Indicators:**
- Use subtle borders (1px, 10% opacity white) instead of heavy shadows
- Layer shadows with decreasing blur radius for higher elevations
- Consider semi-transparent overlays for modals (rgba(0,0,0,0.7))

---

## 2. Typography Trends

### 2.1 Modern Font Choices for Finance Apps

**Recommended Font Families (2024-2025):**

**Primary UI Fonts:**
- **Inter** - Clean, highly legible, excellent for numbers
- **Mona Sans** - GitHub's font, modern geometric sans-serif
- **Figtree** - Balanced, friendly, great for fintech
- **Roboto Flex** - Material 3 variable font, adaptive weights
- **SF Pro** (iOS) / **Roboto** (Android) - System fonts for native feel

**Display/Brand Fonts:**
- **Financier** - Serif used by Financial Times, conveys authority
- **Plus Jakarta Sans** - Modern, geometric, distinctive
- **Outfit** - Clean, open, professional

### 2.2 Number/Money Display Typography

**Best Practices for Financial Data:**
- **Tabular figures** (monospaced numbers) for alignment in lists
- **Bold, large typography** for balances - ensure quick scanning
- **Exaggerated letterforms** for key metrics
- **High contrast** between amounts and currency symbols

**Number Display Hierarchy:**
```
Balance Display:     48sp, Bold, Letter spacing -0.5%
Transaction Amount:  20sp, SemiBold, Tabular figures
Currency Symbol:     75% size of numbers, superscript position
Percentage Change:   16sp, Medium, with +/- indicators
```

**Variable Font Benefits:**
- Single file for all weights (100-900)
- Seamless responsiveness across device sizes
- Performance optimization without style loss
- Adaptive weight based on dynamic type settings

### 2.3 Typography Hierarchy Patterns

**Material 3 Type Scale (Adapted for Finance):**

| Style | Font Size | Weight | Line Height | Use Case |
|-------|-----------|--------|-------------|----------|
| Display Large | 57sp | Regular | 64sp | Hero balances |
| Display Medium | 45sp | Regular | 52sp | Account totals |
| Display Small | 36sp | Regular | 44sp | Section headers |
| Headline Large | 32sp | Medium | 40sp | Screen titles |
| Headline Medium | 28sp | Medium | 36sp | Card titles |
| Headline Small | 24sp | Medium | 32sp | Subsection titles |
| Title Large | 22sp | Medium | 28sp | List headers |
| Title Medium | 16sp | Medium | 24sp | Card subtitles |
| Title Small | 14sp | Medium | 20sp | Item labels |
| Body Large | 16sp | Regular | 24sp | Primary content |
| Body Medium | 14sp | Regular | 20sp | Secondary content |
| Body Small | 12sp | Regular | 16sp | Captions, timestamps |
| Label Large | 14sp | Medium | 20sp | Buttons |
| Label Medium | 12sp | Medium | 16sp | Small buttons |
| Label Small | 11sp | Medium | 16sp | Tags, badges |

---

## 3. Layout & Component Trends

### 3.1 Card Design Evolution

**Current Trend: Minimalism Over Morphism**
While glassmorphism and neumorphism were hyped, 2024-2025 fintech apps favor **clean, flat designs with subtle depth indicators**.

**Card Design Patterns:**

**1. Flat Cards with Subtle Borders:**
```
Background: surface color at elevation 1
Border: 1px solid rgba(255,255,255,0.1)
Border-radius: 16dp (mobile), 24dp (tablet)
Padding: 16dp-24dp
```

**2. Gradient Accent Cards:**
```
Background: gradient from brand color (5% opacity) to surface
Border: none or subtle glow
Used for: Premium accounts, featured sections
```

**3. Numberless Card Design (Physical + Digital):**
Following Apple Card's lead, digital cards display:
- No visible card numbers by default
- Tap to reveal with biometric/auth
- Clean vertical or horizontal layouts
- Brand color as primary identifier

**Corner Radius Guidelines:**
- Small elements (buttons, chips): 8dp-12dp
- Cards, sheets: 16dp-20dp
- Bottom sheets, modals: 24dp-28dp (top corners only)
- Full-screen cards: 0dp or 32dp

### 3.2 Dashboard Layout Patterns

**The "At a Glance" Dashboard:**
Modern finance apps prioritize immediate information access:

**Layout Structure:**
```
┌─────────────────────────────────────┐
│  Greeting + Profile        [Bell]   │  (Compact header)
├─────────────────────────────────────┤
│                                     │
│   ┌─────────────────────────────┐   │
│   │     Total Balance           │   │  (Hero card)
│   │     $24,500.00              │   │
│   │     [+2.4% this month]      │   │
│   └─────────────────────────────┘   │
│                                     │
│   [Quick Actions Row]               │  (Horizontal scroll)
│   [Send] [Request] [Top Up] [More]  │
│                                     │
│   ┌──────────┐  ┌──────────┐       │
│   │ Card 1   │  │ Card 2   │       │  (Account cards)
│   │ ••••1234│  │ ••••5678│       │  (Horizontal scroll)
│   └──────────┘  └──────────┘       │
│                                     │
│   Recent Transactions               │  (Vertical list)
│   ┌─────────────────────────────┐   │
│   │ [Icon] Merchant        -$45 │   │
│   │ [Icon] Deposit        +$200 │   │
│   └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

**Key Principles:**
- Progressive disclosure - show essentials first
- Group related data (balance with trend indicator)
- Horizontal scroll for multiple accounts/cards
- Vertical scroll for transaction history

### 3.3 List Item Designs

**Transaction List Item Pattern:**
```
┌─────────────────────────────────────────────┐
│ ┌────┐  Merchant Name          -$45.00   │
│ │Icon│  Category • Time         [Pending] │
│ └────┘                                      │
└─────────────────────────────────────────────┘
```

**Design Specifications:**
- Icon container: 48dp × 48dp, rounded 12dp, tinted background
- Primary text: Body Large (16sp), Medium weight
- Secondary text: Body Small (12sp), 60% opacity
- Amount: Title Medium (16sp), Bold for debits
- Divider: 1dp hairline, 8% opacity, inset 72dp from left

**Swipe Actions:**
- Left swipe: Quick actions (categorize, hide, details)
- Right swipe: Mark as reviewed, flag
- Peek hint: Show action icons at 20% swipe threshold

### 3.4 Bottom Sheet Usage

**Bottom Sheet Patterns in Finance Apps:**

**1. Transaction Details Sheet:**
- Shows full transaction info
- Includes categorization options
- Actions: Split, Edit, Attach receipt

**2. Quick Actions Sheet:**
- Contextual actions for accounts
- Transfer, pay, settings
- Fixed height (50-70% of screen)

**3. Filter/Filter Sheet:**
- Date range selection
- Category filtering
- Amount range sliders

**Design Guidelines:**
- Top corners: 24dp-28dp radius
- Drag handle: 32dp × 4dp, 12% opacity, centered
- Backdrop scrim: rgba(0,0,0,0.6) with fade animation
- Peek height: Show partial content to indicate scrollability

### 3.5 Floating Action Button (FAB) Alternatives

**Modern Alternatives to FAB:**

**1. Contextual Action Bar (CAB):**
- Appears in context (e.g., account selection)
- "New Transaction" button in header
- Prominent primary button at bottom of lists

**2. Swipe-Up Gestures on Cards:**
- Expand card to reveal actions
- Natural gesture on mobile
- Reduces visual clutter

**3. Quick Action Row:**
```
┌─────────────────────────────────────┐
│ [💸 Send] [💰 Req] [⬆️ Top] [⋯ More]│
└─────────────────────────────────────┘
```
- Horizontal scrollable row
- Icon + label below
- Context-aware based on screen

**4. Contextual Menu:**
- Long-press to reveal options
- Radial menu for quick actions
- Haptic feedback on activation

### 3.6 Navigation Patterns

**Bottom Navigation (Primary):**
```
┌─────────────────────────────────────┐
│  🏠      📊      💳      🔔      ⚙️  │
│ Home  Insights Cards Alerts Settings│
└─────────────────────────────────────┘
```
- 3-5 items maximum
- Active state: Filled icon + label
- Inactive: Outlined icon only or muted
- Haptic feedback on selection

**Top Navigation Alternatives:**
- Segmented control for sub-views
- Tab bar for account switching
- Collapsible header with scroll

**Navigation Rail (Tablets):**
- Side navigation for larger screens
- Icons + labels in vertical arrangement
- Active indicator pill or background

---

## 4. Interaction Trends

### 4.1 Micro-interactions for Financial Data

**Purposeful Micro-interactions:**

**1. Balance Updates:**
- Number counting animation on load
- Pulse effect on significant changes
- Color flash (green for gain, red for loss)

**2. Transaction Completion:**
- Checkmark morphing animation
- Success haptic pattern
- Confetti for milestones (optional)

**3. Pull-to-Refresh:**
- Custom animated spinner (brand-aligned)
- Progress indicator with percentage
- Haptic feedback on completion

**4. Button States:**
- Pressed state: Scale down to 0.96
- Loading state: Spinner replaces label
- Success state: Checkmark morphs from label

### 4.2 Chart/Animation Trends

**Data Visualization Trends:**

**1. Smooth Area Charts:**
- Gradient fills under lines
- Curved corners (cubic interpolation)
- Interactive touch points with tooltips

**2. Bar Charts:**
- Rounded top corners (4dp radius)
- Gradient or solid fills
- Animated entrance (staggered bars)

**3. Donut/Pie Charts:**
- Center hole for total or label
- Animated segment reveals
- Interactive segment selection

**4. Sparklines:**
- Mini charts inline with data
- No axes, pure trend visualization
- Color-coded (green/gray/red)

**Animation Principles:**
- Duration: 200-400ms for micro-interactions
- Easing: Ease-out for entrances, ease-in-out for transitions
- Stagger: 30-50ms delay between list items
- Chart animations: 800-1200ms with easing

### 4.3 Gesture-Based Interactions

**Common Gesture Patterns:**

| Gesture | Action | Implementation |
|---------|--------|----------------|
| Pull down | Refresh | Standard PTR with haptic |
| Swipe left | Quick actions | Reveal action panel |
| Swipe right | Mark/flag | Snap action or dismiss |
| Long press | Context menu | Haptic + radial menu |
| Double tap | Zoom/expand | For charts and images |
| Pinch | Zoom | Transaction history, charts |
| Edge swipe | Navigation | Back or open drawer |

**Best Practices:**
- Always provide visual affordances
- Use haptic feedback to confirm actions
- Support accessibility alternatives
- Avoid gesture conflicts

### 4.4 Haptic Feedback Patterns

**Haptic Patterns by Context:**

**Success/Positive Actions:**
- Payment completion: Light impact
- Goal reached: Success notification + medium impact
- Biometric auth success: Light impact

**Caution/Neutral:**
- Biometric prompt: Light impact
- Toggle switch: Selection feedback
- Tab switch: Light impact

**Warning/Negative:**
- Payment failure: Error notification + medium impact
- Invalid input: Error notification + light impact
- Security alert: Heavy impact

**Interaction Feedback:**
- Button press: Light impact
- Scroll snap: Selection feedback
- Pull-to-refresh trigger: Light impact

---

## 5. Visual Elements

### 5.1 Iconography Styles

**Current Trends:**

**1. Outlined Icons (Primary):**
- 2dp stroke weight
- 24dp × 24dp viewport
- Rounded caps and joins
- Used for: Navigation, lists, secondary actions

**2. Filled Icons (Active States):**
- Same geometry as outlined
- Filled shape for selected state
- Used for: Bottom nav active, primary actions

**3. Duotone Icons (Premium/Featured):**
- Two-tone color scheme
- Primary color + 30% opacity secondary
- Used for: Empty states, illustrations, premium features

**Recommended Icon Sets:**
- **Phosphor Icons** - Highly customizable, modern
- **Heroicons** - Clean, consistent
- **Feather Icons** - Minimal, lightweight
- **Material Symbols** - Variable font icons, dynamic weights
- **SF Symbols** (iOS) - System-native

### 5.2 Illustration Usage

**Illustration Styles in Finance Apps:**

**1. Abstract Geometric:**
- Simple shapes, brand colors
- Used for: Empty states, onboarding
- Examples: N26, Revolut

**2. Line Art:**
- Single weight lines
- Minimal color fills
- Used for: Features, education

**3. 3D Elements:**
- Soft shadows, rounded forms
- Isometric or perspective
- Used for: Cards, achievements

**4. Character/Mascot:**
- Friendly, approachable
- Limited animation
- Used for: Onboarding, help

**Usage Guidelines:**
- Consistent style throughout app
- Dark mode versions of all illustrations
- Lottie animations for complex illustrations
- Keep file sizes optimized (WebP, SVG)

### 5.3 Data Visualization Trends

**Modern Visualization Approaches:**

**1. Contextual Color Coding:**
- Green for income/savings
- Red/orange for expenses (use sparingly)
- Gray for neutral/projected

**2. Interactive Elements:**
- Tap to drill down
- Long press for details
- Pinch to zoom time ranges

**3. Real-time Indicators:**
- Live pulse indicators
- Animated updates
- Push notification previews

**4. Simplified Complexity:**
- Hide detailed data behind interactions
- Progressive disclosure
- Smart defaults for time ranges

### 5.4 Skeleton Loading Patterns

**Skeleton Screen Design:**

**Structure:**
```
┌─────────────────────────────────────┐
│ ┌────┐  ┌──────────────┐            │  (Avatar + title)
│ │████│  │██████████████│            │  (Shimmer effect)
│ └────┘  └──────────────┘            │
│                                     │
│ ┌─────────────────────────────────┐ │  (Card placeholder)
│ │█████████████████████████████████│ │
│ │█████████████████████████████████│ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌────┐ ┌────────────────────────┐  │  (List items)
│ │████│ │███████████████████████│  │
│ └────┘ │███████████████████████│  │
└─────────────────────────────────────┘
```

**Shimmer Effect Specifications:**
- Background: Surface color at current elevation
- Shimmer: Linear gradient from transparent → 15% white → transparent
- Animation: 1.5s duration, infinite, ease-in-out
- Direction: Left to right (or top to bottom for vertical lists)

**Implementation Best Practices:**
- Match final layout dimensions exactly
- Use rounded corners matching final content
- Animate smoothly without jarring transitions
- Stop shimmer when content loads (even if partially)
- Show for minimum 300ms to prevent flashing

---

## 6. Accessibility Trends

### 6.1 High Contrast Considerations

**WCAG 2.1 Level AA Requirements:**

**Contrast Ratios:**
| Element | Minimum Ratio | Enhanced |
|---------|---------------|----------|
| Normal text (<18sp) | 4.5:1 | 7:1 |
| Large text (≥18sp bold) | 3:1 | 4.5:1 |
| UI components | 3:1 | - |
| Graphical objects | 3:1 | - |

**High Contrast Mode:**
- Provide system-aware high contrast theme
- Increase border visibility
- Enhance focus indicators
- Ensure text remains readable over images

### 6.2 Dynamic Type Support

**Android Implementation:**
```kotlin
// Use sp units for all text
// Support system font scale settings
Text(
    text = "Balance",
    fontSize = 16.sp,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
)
```

**iOS Implementation:**
- Support Dynamic Type (UIFontMetrics)
- Test at all accessibility text sizes
- Ensure layouts adapt gracefully
- Use readableContentWidth for text containers

**Best Practices:**
- Use scalable text units (sp on Android, Dynamic Type on iOS)
- Test at 200% text scaling
- Ensure buttons remain tappable at large sizes
- Avoid fixed-height containers for text

### 6.3 Screen Reader Optimization

**Content Labels:**
```kotlin
// Android - Content descriptions
Icon(
    imageVector = Icons.Default.AccountBalance,
    contentDescription = "Checking account ending in 1234, balance $2,450"
)

// Clear grouping for complex elements
Box(
    modifier = Modifier
        .semantics(mergeDescendants = true) {}
        .clickable { /* action */ }
) {
    // Content grouped as single element
}
```

**Navigation:**
- Logical focus order (top-to-bottom, left-to-right)
- Skip navigation options
- Heading hierarchy for screen structure
- Landmarks for major sections

**Charts and Visualizations:**
- Provide data tables as alternatives
- Use contentDescription for key data points
- Offer list view alternatives to charts
- Announce significant changes

**Touch Targets:**
- Minimum 48dp × 48dp touch targets
- Adequate spacing between interactive elements
- Motor accessibility considerations
- Multiple authentication options (not just biometrics)

---

## 7. Specific App Examples & Analysis

### 7.1 Revolut

**Key Design Characteristics:**
- Gradient hero cards with account balances
- Bottom navigation with clear iconography
- Dark mode-first design philosophy
- Subtle animations for transactions
- Category icons with consistent styling

**Color Palette:**
- Primary: Blue gradients
- Surfaces: Pure black to dark gray
- Accents: Blue, purple, pink for different features

### 7.2 N26

**Key Design Characteristics:**
- Ultra-minimalist interface
- Vertical space utilization
- Transparent/white card designs
- Teal accent color throughout
- Clean typography hierarchy

**Notable Features:**
- Spaces (sub-accounts) with visual differentiation
- Smooth micro-interactions
- Biometric-first security UX

### 7.3 Monzo

**Key Design Characteristics:**
- Hot coral (#FF4D4D) signature color
- Friendly, approachable tone
- Strong visual hierarchy
- Transaction feed with rich data
- Savings pots with goal visualization

**Design Philosophy:**
- Social media-inspired feed design
- Emoji and customization support
- Transparent spending insights

### 7.4 Robinhood

**Key Design Characteristics:**
- Green primary for growth/positive
- Simplified trading interface
- Real-time data visualization
- Confetti animation for first trades
- Clean, minimal information density

**Visual Elements:**
- Line charts with gradient fills
- Simple iconography
- Dark mode optimized

### 7.5 Coinbase

**Key Design Characteristics:**
- Blue primary color for trust
- Crypto-focused visual language
- Real-time price tickers
- Portfolio visualization with pie charts
- Educational content integration

---

## 8. Recommendations for Pyera Finance

### 8.1 Immediate Implementation Priorities

**Phase 1 - Foundation:**
1. Implement proper dark mode with surface elevation system
2. Adopt Material 3 typography scale with Inter font
3. Standardize card designs with 16dp radius and subtle borders
4. Create consistent icon set (outlined primary, filled active)

**Phase 2 - Enhancement:**
1. Add shimmer skeleton loading screens
2. Implement micro-interactions for transactions
3. Create data visualization components (charts, sparklines)
4. Add haptic feedback for key actions

**Phase 3 - Polish:**
1. Implement gesture-based interactions
2. Add custom illustrations for empty states
3. Create advanced animations for data updates
4. Full accessibility audit and improvements

### 8.2 Color Scheme Recommendation

**Pyera Brand Palette:**
```
Primary:        #007AFF (Trust Blue)
Primary Dark:   #0056CC
Secondary:      #34C759 (Growth Green)
Accent:         #FF9500 (Warm Orange)

Dark Theme:
  Background:     #0A0A0F
  Surface:        #1C1C1E
  Surface High:   #2C2C2E
  Text Primary:   #FFFFFF
  Text Secondary: rgba(255,255,255,0.7)
  Text Tertiary:  rgba(255,255,255,0.5)
```

### 8.3 Typography Stack

**Font Family:** Inter (variable font)
**Fallback stack:** Inter, -apple-system, BlinkMacSystemFont, sans-serif

**Scale Implementation:**
- Use Material 3 type scale
- Implement dynamic type support
- Tabular figures for all numeric displays
- Negative letter-spacing for large displays

### 8.4 Component Library Priorities

**High Priority Components:**
1. Transaction list item with swipe actions
2. Account balance card with trend indicator
3. Quick action buttons row
4. Bottom sheet variants
5. Skeleton loading placeholders

**Medium Priority:**
1. Data visualization charts
2. Filter and sort components
3. Empty state illustrations
4. Toast/snackbar notifications
5. Dialog and modal patterns

---

## 9. Resources & References

### Design Systems to Study
- [Material Design 3](https://m3.material.io/)
- [Apple Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
- [IBM Carbon Design System](https://carbondesignsystem.com/)
- [Atlassian Design System](https://atlassian.design/)

### Tools & Libraries
- **Icons:** Phosphor Icons, Material Symbols, SF Symbols
- **Illustrations:** unDraw, Storyset, LottieFiles
- **Prototyping:** Figma, Framer, Principle
- **Animation:** Lottie, Rive, Jetpack Compose Animation

### Further Reading
- WCAG 2.2 Guidelines: https://www.w3.org/WAI/WCAG22/quickref/
- Material Motion Guidelines: https://m3.material.io/styles/motion/overview
- iOS Accessibility: https://developer.apple.com/accessibility/

---

*Document Version: 1.0*  
*Last Updated: February 2025*  
*Prepared for: Pyera Finance Redesign Project*
