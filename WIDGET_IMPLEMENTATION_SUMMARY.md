# Pyera Finance - Home Screen Widgets Implementation

## Overview
This document summarizes the implementation of Glance-based home screen widgets for the Pyera Finance Android app.

## Widgets Implemented

### 1. Balance Widget (2x1)
- **File**: `BalanceWidget.kt`
- **Receiver**: `BalanceWidgetReceiver`
- **Size**: 2x1 cells (180dp x 110dp)
- **Features**:
  - Displays total balance
  - Shows monthly income (↑) and expense (↓) summary
  - Tap to open app dashboard
  - Dark/light theme support

### 2. Quick Add Widget (3x1)
- **File**: `QuickAddWidget.kt`
- **Receiver**: `QuickAddWidgetReceiver`
- **Size**: 3x1 cells (250dp x 110dp)
- **Features**:
  - Displays current balance
  - "+ Income" button (green)
  - "+ Expense" button (red)
  - Quick access to add transactions
  - Dark/light theme support

### 3. Transactions Widget (4x2)
- **File**: `TransactionsWidget.kt`
- **Receiver**: `TransactionsWidgetReceiver`
- **Size**: 4x2 cells (320dp x 220dp)
- **Features**:
  - Displays total balance with income/expense summary
  - Shows up to 5 recent transactions
  - Each transaction shows category icon, name, date, and amount
  - Color-coded amounts (green for income, red for expense)
  - Scrollable transaction list
  - Dark/light theme support

## File Structure

```
app/src/main/java/com/pyera/app/widget/
├── BalanceWidget.kt              # Balance widget UI
├── QuickAddWidget.kt             # Quick add widget UI
├── TransactionsWidget.kt         # Transactions widget UI
├── BalanceWidgetReceiver.kt      # Widget receivers
├── WidgetDataProvider.kt         # Data fetching logic
├── WidgetPreferences.kt          # Widget settings storage
├── WidgetUpdateWorker.kt         # Background update worker
└── WidgetConfigurationActivity.kt # Configuration screen

app/src/main/res/xml/
├── balance_widget_info.xml       # Balance widget metadata
├── quick_add_widget_info.xml     # Quick add widget metadata
└── transactions_widget_info.xml  # Transactions widget metadata

app/src/main/res/layout/
└── widget_glance_container.xml   # Initial loading layout

app/src/main/res/values/
├── colors.xml                    # Widget color definitions
└── strings.xml                   # Widget strings

app/src/main/res/drawable/
├── widget_balance_preview.xml    # Balance widget preview
├── widget_quick_add_preview.xml  # Quick add widget preview
└── widget_transactions_preview.xml # Transactions widget preview
```

## Dependencies Added

```kotlin
// Glance Widgets
implementation("androidx.glance:glance:1.0.0")
implementation("androidx.glance:glance-appwidget:1.0.0")
implementation("androidx.glance:glance-material3:1.0.0")
```

## AndroidManifest.xml Updates

Added to `<application>`:

```xml
<!-- Widget Configuration Activity -->
<activity
    android:name=".widget.WidgetConfigurationActivity"
    android:exported="false"
    android:theme="@style/Theme.Pyera">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>

<!-- Balance Widget Receiver -->
<receiver
    android:name=".widget.BalanceWidgetReceiver"
    android:exported="true"
    android:label="Pyera Balance">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/balance_widget_info" />
</receiver>

<!-- Quick Add Widget Receiver -->
<receiver
    android:name=".widget.QuickAddWidgetReceiver"
    android:exported="true"
    android:label="Pyera Quick Add">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/quick_add_widget_info" />
</receiver>

<!-- Transactions Widget Receiver -->
<receiver
    android:name=".widget.TransactionsWidgetReceiver"
    android:exported="true"
    android:label="Pyera Transactions">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/transactions_widget_info" />
</receiver>
```

## Widget Configuration Options

Available in `WidgetConfigurationActivity`:

1. **Theme Selection**: Dark or Light theme
2. **Account Selection**: Choose which account to display data from
3. **Update Frequency**: 15-120 minutes (default: 30 minutes)
4. **Display Options**: Show/hide balance amount

## Data Flow

1. Widgets use `WidgetDataProvider` to fetch data from Room database
2. Database is accessed using SQLCipher (same encryption as main app)
3. Balance data includes:
   - Total balance (all transactions)
   - Monthly income (current month only)
   - Monthly expense (current month only)
4. Transactions are sorted by date (newest first) and limited to 5 items

## Widget Updates

### Automatic Updates
- WorkManager schedules periodic updates based on user preference
- Default interval: 30 minutes
- Updates are battery-efficient (won't run when battery is low)

### Manual Updates
- Call `WidgetUpdateWorker.updateNow(context)` to force immediate update
- Useful when new transactions are added

## Tap Actions

| Widget | Tap Action |
|--------|-----------|
| Balance Widget | Opens app dashboard |
| Quick Add - Income | Opens add transaction with type=INCOME |
| Quick Add - Expense | Opens add transaction with type=EXPENSE |
| Transactions Widget | Opens transactions list |

## Testing Checklist

- [ ] Widgets appear in widget picker
- [ ] Widgets display correct balance data
- [ ] Widgets update when data changes
- [ ] Tap actions work correctly
- [ ] Configuration screen opens on widget add
- [ ] Theme changes apply correctly
- [ ] Widget survives device reboot
- [ ] Widget survives app update

## Known Limitations

1. Widgets require the user to be logged in to display data
2. SQLCipher passphrase must be available for database access
3. Widget updates are subject to Android's Doze mode and App Standby
4. Very large transaction lists may be truncated

## Future Enhancements

1. Add support for account-specific widgets
2. Add weekly/monthly spending summary widget
3. Add budget progress widget
4. Support for widget resizing beyond current limits
5. Interactive charts in larger widget sizes
