# Pyera Project Setup Guide

Complete guide to set up and run the Pyera Finance App on your local machine.

## 📋 Prerequisites

- **Android Studio**: Latest stable version (Giraffe or later)
- **JDK**: Version 17 or later
- **Android SDK**: API 34 (Android 14)
- **Git**: For version control

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Pyera
```

### 2. Check Project Structure

Ensure you have these key files:
```
Pyera/
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json          ← Firebase config (you need to add this)
│   └── src/...
├── build.gradle.kts
├── gradle/
├── gradlew / gradlew.bat
└── settings.gradle.kts
```

---

## 🔥 Firebase Setup (Required for Auth)

### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Name your project: `pyera-finance` (or any name)
4. Disable Google Analytics (optional)
5. Click **"Create project"**

### Step 2: Register Android App

1. Click the **Android icon** (</>) to add an app
2. Enter package name: `com.pyera.app`
3. Enter app nickname: `Pyera`
4. Skip debug signing certificate (optional for development)
5. Click **"Register app"**

### Step 3: Download Config File

1. Click **"Download google-services.json"**
2. Move the file to your project's `app/` folder:
   ```
   Pyera/app/google-services.json
   ```

### Step 4: Enable Authentication Providers

1. In Firebase Console, go to **Build → Authentication**
2. Click **"Get started"**
3. Enable **"Email/Password"** provider
   - Toggle **"Enable"** and save
4. Enable **"Google"** provider (for Google Sign-In)
   - Toggle **"Enable"**
   - Add support email
   - Click **"Save"**

---

## 🔑 API Keys Setup

### Kimi API (Optional - For AI Chat)

The app uses Kimi (Moonshot AI) for the chat assistant feature. The API key has already been configured in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "KIMI_API_KEY", "\"sk-kimi-...\"")
```

**To use your own key:**
1. Get an API key from [Moonshot AI Platform](https://platform.moonshot.cn/)
2. Update in `app/build.gradle.kts`

**Note**: The chat feature requires internet connection to work.

> ⚠️ **Note**: The app will work without this, but receipt scanning won't function.

---

## 🔐 Google Sign-In Setup (Optional but Recommended)

The app now includes full Google Sign-In support alongside Email/Password.

### Step 1: Already Done ✅
- Dependency added: `play-services-auth:20.7.0`
- Code implemented in `LoginScreen.kt`
- Firebase configured with Web Client ID

### Step 2: Configure SHA-1 Fingerprint (Required)

For Google Sign-In to work, you need to add your app's SHA-1 fingerprint:

**Get your debug fingerprint:**
```bash
./gradlew signingReport
```

Look for:
```
Variant: debug
Config: debug
SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
```

**Add to Firebase:**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Project Settings → Your apps
3. Click **"Add fingerprint"**
4. Paste your SHA-1
5. Click **"Save"**
6. Download new `google-services.json`
7. Replace the file in your project

### Step 3: Configure OAuth Consent Screen (Required)

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project: `pyera-96753`
3. Go to **APIs & Services → OAuth consent screen**
4. Select **"External"** (or "Internal" if G Suite)
5. Fill in required fields:
   - App name: `Pyera`
   - User support email: your email
   - Developer contact: your email
6. Click **"Save and Continue"**
7. Click **"Save and Continue"** (Scopes)
8. Click **"Save and Continue"** (Test users)
9. Click **"Back to Dashboard"**
10. Click **"PUBLISH APP"** (or keep in testing for development)

### Step 4: Test

1. Rebuild the app
2. Tap **"Continue with Google"**
3. Select your Google account
4. You should be logged in!

### Troubleshooting

**Error: `12500: Developer error`**
→ SHA-1 fingerprint not added to Firebase

**Error: `10:` or login fails**
→ OAuth consent screen not configured

**Error: `16:` or cancelled**
→ User cancelled the sign-in

**Build error: Cannot find GoogleSignIn**
→ Sync project: `File → Sync Project with Gradle Files`

---

## 🛠️ Build Configuration

### Check local.properties

Ensure you have:
```properties
sdk.dir=C:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
```

Or on Mac/Linux:
```properties
sdk.dir=/Users/<YourUsername>/Library/Android/sdk
```

### Sync Project

1. Open Android Studio
2. Click **"Sync Now"** in the notification bar
3. Wait for Gradle sync to complete

---

## ▶️ Running the App

### Option 1: Android Studio

1. Connect your Android device or start an emulator
2. Click the **"Run"** button (▶️) or press `Shift + F10`
3. Select your device

### Option 2: Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Or both in one command
./gradlew assembleDebug installDebug
```

---

## 🧪 Testing the App

### First Launch Experience

1. **Onboarding**: You'll see 4 introduction slides
   - Swipe or tap "Next" to navigate
   - Tap "Skip" to go directly to login
   - Tap "Get Started" on the last slide

2. **Login/Register**: 
   - Use the Register tab to create an account
   - Or use any email/password (Firebase handles auth)

3. **Dashboard**:
   - Pre-populated with sample transactions
   - Shows real balance calculated from data
   - Recent transactions list

### Pre-populated Data

The app automatically creates:
- **13 Categories** (Food, Transport, Salary, etc.)
- **12 Sample Transactions** (income and expenses)

### Reset Data

To clear all data and start fresh:
1. Go to **Profile → Settings**
2. Look for "Clear Data" option (if implemented)
3. Or uninstall and reinstall the app

---

## 🔧 Troubleshooting

### Build Errors

#### Error: `google-services.json` is missing
```
File google-services.json is missing. 
The Google Services Plugin cannot function without it.
```
**Solution**: Follow Firebase Setup steps above.

#### Error: `GEMINI_API_KEY` not found
```
Unresolved reference: GEMINI_API_KEY
```
**Solution**: Add your API key to `app/build.gradle.kts` or use the placeholder.

#### Error: Gradle sync failed
```
Could not find com.android.tools.build:gradle:...
```
**Solution**: 
- Check internet connection
- Update Android Studio
- Try: `File → Invalidate Caches → Invalidate and Restart`

### Runtime Errors

#### Error: `FirebaseAuth` not initialized
```
Default FirebaseApp is not initialized in this process
```
**Solution**: 
- Ensure `google-services.json` is in `app/` folder
- Check package name matches Firebase registration
- Clean and rebuild: `./gradlew clean build`

#### Error: Network requests fail
**Solution**: 
- Check internet connection
- Ensure Firebase project is active
- Check if API keys are valid

---

## 📱 Development Tips

### Skip Onboarding for Development

To bypass onboarding during development, modify `MainActivity.kt`:

```kotlin
// Temporarily force skip onboarding
val startDestination = Screen.Auth.Login.route  // Instead of checking prefs
```

### View Database (Debug)

Use Android Studio's **App Inspector**:
1. Run app in debug mode
2. View → Tool Windows → App Inspection
3. Select Database Inspector
4. Browse tables: `transactions`, `categories`, etc.

### Debug Pre-populated Data

Add this to your code temporarily:
```kotlin
// In MainActivity or Dashboard
val dataSeeder = LocalDataSeeder(context, database)
Log.d("DEBUG", "Data seeded: ${dataSeeder.isDataSeeded()}")
Log.d("DEBUG", "Onboarding completed: ${dataSeeder.isOnboardingCompleted()}")
```

---

## 📝 File Structure Reference

```
app/src/main/java/com/pyera/app/
├── data/
│   ├── local/
│   │   ├── LocalDataSeeder.kt       ← Pre-populated data
│   │   ├── PyeraDatabase.kt
│   │   └── entity/
│   └── repository/
├── ui/
│   ├── onboarding/                  ← NEW
│   │   └── OnboardingScreen.kt
│   ├── auth/
│   ├── dashboard/
│   │   └── DashboardViewModel.kt    ← NEW
│   ├── transaction/
│   ├── debt/
│   ├── profile/
│   ├── components/
│   ├── navigation/
│   └── theme/
├── MainActivity.kt
└── PyeraApp.kt
```

---

## ✅ Setup Verification Checklist

- [ ] Firebase project created
- [ ] `google-services.json` downloaded and placed in `app/`
- [ ] Email/Password auth enabled in Firebase
- [ ] Gemini API key added (optional)
- [ ] `local.properties` has correct SDK path
- [ ] Gradle sync completed successfully
- [ ] App builds without errors
- [ ] App installs on device/emulator
- [ ] Onboarding screens display correctly
- [ ] Login/Register works
- [ ] Dashboard shows pre-populated data

---

## 🆘 Getting Help

If you encounter issues:

1. Check the **error log** in Android Studio
2. Verify **all setup steps** are complete
3. Try **clean build**: `./gradlew clean`
4. Check **Firebase Console** for any warnings
5. Review **this guide** for common issues

---

## 📚 Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)

---

**You're all set!** 🎉 Build and run the app to see the new onboarding and pre-populated data features.
