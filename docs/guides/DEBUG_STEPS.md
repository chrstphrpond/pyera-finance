# Google Sign-In Debug Steps

## ✅ Changes Made

I've added comprehensive logging to your app to help diagnose the issue:

### Files Modified:
1. `GoogleAuthHelper.kt` - Added detailed logging for all sign-in operations
2. `LoginScreen.kt` - Added logging for button clicks and activity results

### Created Guide:
- `GOOGLE_SIGNIN_FIX_GUIDE.md` - Step-by-step OAuth configuration guide

---

## 🚀 Next Steps to Fix Google Sign-In

### STEP 1: Configure OAuth Consent Screen (CRITICAL)

This is the #1 cause of Google Sign-In failures.

1. Go to: https://console.cloud.google.com/apis/credentials/consent?project=pyera-11128
2. If not configured:
   - Click "GET STARTED"
   - App name: `Pyera`
   - User support email: [your-email]
   - Click through all screens
   - Click "PUBLISH APP"

### STEP 2: Enable Google Provider in Firebase

1. Go to: https://console.firebase.google.com/project/pyera-11128/authentication/providers
2. Toggle Google provider to **ENABLE**
3. Add support email
4. Click **SAVE**

### STEP 3: Clean Build & Test

```bash
# Uninstall app
adb uninstall com.pyera.app

# Clean and rebuild
.\gradlew clean assembleDebug
```

---

## 🔍 How to Debug

### Check Logcat in Android Studio:

1. Run the app
2. Open **Logcat** tab (bottom of Android Studio)
3. Set filter: `tag:GoogleSignIn`
4. Tap "Continue with Google" button
5. Watch for log messages

### Expected Log Flow (When Working):
```
D/GoogleSignIn: Google Sign-In button clicked
D/GoogleSignIn: Sign-in intent created successfully
D/GoogleSignIn: Sign-in activity result received: resultCode=-1
D/GoogleSignIn: Result OK, handling sign-in data
D/GoogleSignIn: Account retrieved: [name], email: [email]
D/GoogleSignIn: ID Token obtained successfully
```

### Error Log Examples:

#### If OAuth consent not configured:
```
E/GoogleSignIn: OAuth consent screen not configured (12500)
```
**Fix:** Follow STEP 1 above

#### If SHA-1 mismatch:
```
E/GoogleSignIn: Developer error (10): SHA-1 or package name mismatch
```
**Fix:** Add SHA-1 to Firebase console

#### If Google provider disabled:
```
E/GoogleSignIn: Sign-in failed: [firebase error]
```
**Fix:** Follow STEP 2 above

---

## 📱 Testing Checklist

Before each test:
- [ ] Uninstall app from device/emulator
- [ ] Clean build in Android Studio
- [ ] Run fresh install

During test:
- [ ] Watch Logcat for GoogleSignIn tag
- [ ] Note any error codes
- [ ] Screenshot error messages if shown

---

## 🆘 Still Not Working?

If you've completed all steps and it's still failing:

1. **Copy the exact Logcat output** when you try to sign in
2. **Share the error code** (12500, 10, etc.)
3. **Check if you see the Google account picker** at all

Common issues:
- **No account picker shown** → OAuth consent screen issue
- **Account picker shown, then fails** → Firebase Auth provider issue
- **Works on emulator but not device** → Device-specific issue (check Play Services)

---

## 📞 Quick Links

- OAuth Consent Screen: https://console.cloud.google.com/apis/credentials/consent?project=pyera-11128
- Firebase Auth: https://console.firebase.google.com/project/pyera-11128/authentication/providers
- Firebase Settings: https://console.firebase.google.com/project/pyera-11128/settings/general
