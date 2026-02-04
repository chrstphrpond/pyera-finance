# Google Sign-In Fix Guide for Pyera

## 🔴 CRITICAL FIX #1: OAuth Consent Screen (MOST LIKELY CAUSE)

### Step 1: Open Google Cloud Console
1. Go to: https://console.cloud.google.com/apis/credentials/consent?project=pyera-11128
2. Make sure you're logged in with the Google account that owns the Firebase project

### Step 2: Check Current Status
- If you see **"Not configured"** → You need to set it up
- If you see **"Testing"** → You need to publish it
- If you see **"In production"** ✅ → This is not the issue

### Step 3: Configure OAuth Consent Screen
1. Click **"GET STARTED"** or **"EDIT"**
2. Fill in the form:
   ```
   App name: Pyera
   User support email: [your-email]@gmail.com
   ```
3. Click **"SAVE AND CONTINUE"**
4. On Scopes page → Click **"SAVE AND CONTINUE"** (no changes needed)
5. On Test users page → Click **"SAVE AND CONTINUE"** (no changes needed)
6. On Summary page → Click **"BACK TO DASHBOARD"**

### Step 4: Publish the App
1. On the OAuth consent screen dashboard
2. Click **"PUBLISH APP"** button
3. Confirm the dialog
4. Status should now show **"In production"**

---

## 🔴 CRITICAL FIX #2: Enable Google Provider in Firebase

### Step 1: Open Firebase Console
1. Go to: https://console.firebase.google.com/project/pyera-11128/authentication/providers

### Step 2: Enable Google Sign-In
1. Find **"Google"** in the list of providers
2. Click the toggle to **ENABLE**
3. Add a **Support email for project** (your email)
4. Click **SAVE**

---

## 🔴 CRITICAL FIX #3: Clean Build & Test

### Step 1: Uninstall App
```bash
# In terminal or Android Studio terminal
adb uninstall com.pyera.app
```

### Step 2: Clean Project
In Android Studio:
```
Build → Clean Project
Build → Rebuild Project
```

Or terminal:
```bash
.\gradlew clean assembleDebug
```

### Step 3: Run App
1. Run app on device/emulator
2. Try Google Sign-In
3. Check Logcat for error codes

---

## 🔍 How to Check Logcat for Errors

### In Android Studio:
1. Open **Logcat** tab (bottom of window)
2. Set filter to: `tag:GoogleSignIn`
3. Run app and try Google Sign-In
4. Look for error messages like:
   - `Sign-in failed: 12500`
   - `Sign-in failed: 10`
   - `Developer error`

### Common Error Codes:
| Code | Meaning | Fix |
|------|---------|-----|
| 12500 | OAuth consent not configured | Follow Fix #1 above |
| 10 | SHA-1 mismatch | Already verified ✅ |
| 12501 | User cancelled | Not an error |

---

## ✅ Verification Checklist

After completing the fixes above:

- [ ] OAuth consent screen shows "In production"
- [ ] Google provider is enabled in Firebase
- [ ] App uninstalled and reinstalled
- [ ] Project clean built
- [ ] Test Google Sign-In

---

## 🆘 Still Not Working?

If you've completed all steps and it's still not working:

1. **Get the exact error from Logcat:**
   - Filter: `tag:GoogleSignIn`
   - Copy the error message

2. **Verify Web Client ID matches:**
   - In `google-services.json`: `481947273203-c89a5pa0m6uvis0dobe90tadjukq9tcb.apps.googleusercontent.com`
   - In Firebase Console → Authentication → Google → Web SDK configuration

3. **Check package name:**
   - Must be exactly: `com.pyera.app`

4. **Test on different device/emulator**

---

## 📞 Support Links

- OAuth Consent Screen: https://console.cloud.google.com/apis/credentials/consent?project=pyera-11128
- Firebase Auth Providers: https://console.firebase.google.com/project/pyera-11128/authentication/providers
- Firebase Project Settings: https://console.firebase.google.com/project/pyera-11128/settings/general
