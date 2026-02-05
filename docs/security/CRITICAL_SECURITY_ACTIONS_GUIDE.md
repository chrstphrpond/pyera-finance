# Pyera Finance - Critical Security Actions Guide

**Date:** February 5, 2026  
**Status:** Immediate Action Required

---

## 🔴 Action 1: Rotate Kimi API Key

The Kimi API key has been exposed in your codebase. You must rotate it immediately.

### Step 1: Generate New API Key

1. Go to **Moonshot AI Console**: https://platform.moonshot.cn
2. Sign in with your account
3. Navigate to **API Keys** or **开发者中心 (Developer Center)**
4. Click **Create New Key** or **创建新密钥**
5. Copy the new key (starts with `sk-kimi-...`)

### Step 2: Revoke Old API Key

1. In the same API Keys page, find the old key:
   ```
   sk-kimi-eo71AzD4aHGSIzVny740hvWJloIT0a36Q384X5uusjWsstb1wqO6PBeFOazSbAPQ
   ```
2. Click **Revoke** or **删除 (Delete)** next to the old key
3. Confirm the revocation

### Step 3: Update Your Local Environment

1. Open your `local.properties` file in the project root:
   ```bash
   # Windows
   notepad local.properties
   
   # Mac/Linux
   nano local.properties
   ```

2. Replace the old key with the new one:
   ```properties
   # OLD (REVOKED - DO NOT USE)
   # KIMI_API_KEY=sk-kimi-eo71AzD4aHGSIzVny740hvWJloIT0a36Q384X5uusjWsstb1wqO6PBeFOazSbAPQ
   
   # NEW
   KIMI_API_KEY=sk-kimi-YOUR_NEW_KEY_HERE
   ```

3. Save and close the file

### Step 4: Verify Git Ignore

Ensure `local.properties` is in your `.gitignore`:

```bash
# Check .gitignore
cat .gitignore | grep local.properties

# Should output: local.properties
```

If not present, add it:
```bash
echo "local.properties" >> .gitignore
```

### Step 5: Clean Build and Test

```bash
# Clean and rebuild
./gradlew clean assembleDebug

# Or on Windows
.\gradlew.bat clean assembleDebug
```

Test the chat feature to ensure the new key works.

---

## 🔴 Action 2: Restrict Firebase API Keys

Your Firebase API keys are committed to version control. While this is common practice for Firebase, you should restrict them to prevent abuse.

### Step 1: Find Your Firebase API Keys

1. Open `app/google-services.json`
2. Look for these values:
   ```json
   {
     "client": [{
       "oauth_client": [{
         "client_id": "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
       }]
     }],
     "api_key": [{
       "current_key": "YOUR_API_KEY"
     }]
   }
   ```

### Step 2: Restrict API Keys in Google Cloud Console

1. Go to **Google Cloud Console**: https://console.cloud.google.com
2. Select your Firebase project
3. Navigate to **APIs & Services** → **Credentials**
4. Find the API key listed in `google-services.json`
5. Click **Edit** (pencil icon)

### Step 3: Add Application Restrictions

1. Under **Application restrictions**, select **Android apps**
2. Click **Add an item**
3. Add your app details:
   - **Package name**: `com.pyera.app`
   - **SHA-1 certificate fingerprint**: (see below)

#### Get Your SHA-1 Fingerprints:

**Debug Key:**
```bash
# On Mac/Linux
keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore

# On Windows
keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore
```
Password: `android`

**Release Key:**
```bash
# If you have a release keystore
keytool -list -v -alias YOUR_ALIAS -keystore /path/to/release.keystore
```

4. Click **Done** then **Save**

### Step 4: Add API Restrictions

1. Under **API restrictions**, select **Restrict key**
2. Select only these APIs:
   - ✅ Firebase Installations API
   - ✅ Identity Toolkit API
   - ✅ Token Service API
   - ✅ Firebase Cloud Messaging API (if using push notifications)
3. Click **Save**

### Step 5: Verify Restrictions Work

1. Clean build your app:
   ```bash
   ./gradlew clean assembleDebug
   ```

2. Test Firebase features:
   - Google Sign-In
   - Firestore read/write
   - Firebase Auth

---

## 🔴 Action 3: Get Actual Certificate Pins

The certificate pinning is configured with placeholder pins. You need to replace them with actual SHA-256 pins.

### Option A: Using OpenSSL (Recommended)

**On Mac/Linux:**
```bash
# Get the certificate pin for api.moonshot.cn
openssl s_client -servername api.moonshot.cn -connect api.moonshot.cn:443 2>/dev/null | \
  openssl x509 -pubkey -noout | \
  openssl pkey -pubin -outform der | \
  openssl dgst -sha256 -binary | \
  openssl enc -base64
```

**On Windows (PowerShell):**
```powershell
# First, ensure OpenSSL is installed (comes with Git for Windows)
# Or download from: https://slproweb.com/products/Win32OpenSSL.html

# Run in Git Bash or with OpenSSL in PATH
openssl s_client -servername api.moonshot.cn -connect api.moonshot.cn:443 2>/dev/null | `
  openssl x509 -pubkey -noout | `
  openssl pkey -pubin -outform der | `
  openssl dgst -sha256 -binary | `
  openssl enc -base64
```

**Expected Output:**
```
sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=
```
(Replace with actual output)

### Option B: Using Online Tools (Alternative)

If OpenSSL is not available:

1. Go to https://www.ssllabs.com/ssltest/
2. Enter `api.moonshot.cn`
3. Wait for the scan to complete
4. Look for **Pin SHA256** in the certificate details
5. Copy the pin value

### Step 2: Get Backup Pin

Get a backup pin from a different certificate in the chain:

```bash
# Get all certificates in the chain
openssl s_client -servername api.moonshot.cn -connect api.moonshot.cn:443 -showcerts 2>/dev/null
```

Use the intermediate certificate's pin as backup.

### Step 3: Update ChatRepositoryImpl.kt

Open `app/src/main/java/com/pyera/app/data/repository/ChatRepositoryImpl.kt`

Replace the placeholder pins:

```kotlin
// BEFORE (placeholders):
private val certificatePinner = CertificatePinner.Builder()
    .add("api.moonshot.cn", "sha256/PLACEHOLDER_PRIMARY_PIN")
    .add("api.moonshot.cn", "sha256/PLACEHOLDER_BACKUP_PIN")
    .build()

// AFTER (actual pins):
private val certificatePinner = CertificatePinner.Builder()
    .add("api.moonshot.cn", "sha256/ACTUAL_PRIMARY_PIN_HERE")
    .add("api.moonshot.cn", "sha256/ACTUAL_BACKUP_PIN_HERE")
    .build()
```

### Step 4: Test Certificate Pinning

1. Build and run the app:
   ```bash
   ./gradlew installDebug
   ```

2. Test the chat feature - it should work normally

3. Test MITM detection (optional):
   - Install Charles Proxy or mitmproxy
   - Configure device to use the proxy
   - Try to use the chat feature
   - Expected: Connection should fail with certificate pinning error

---

## 📋 Verification Checklist

After completing all actions:

- [ ] **Kimi API Key**
  - [ ] Old key revoked in Moonshot console
  - [ ] New key generated
  - [ ] `local.properties` updated with new key
  - [ ] Chat feature works with new key
  - [ ] `local.properties` is in `.gitignore`

- [ ] **Firebase API Keys**
  - [ ] Restricted to Android apps
  - [ ] Package name `com.pyera.app` added
  - [ ] SHA-1 fingerprints added (debug and release)
  - [ ] API restrictions applied
  - [ ] Firebase features still work

- [ ] **Certificate Pinning**
  - [ ] Primary pin obtained for api.moonshot.cn
  - [ ] Backup pin obtained
  - [ ] ChatRepositoryImpl.kt updated
  - [ ] App builds successfully
  - [ ] Chat feature works

---

## ⚠️ Important Notes

### Kimi API Key Security
- **Never** commit API keys to version control
- Use environment variables in CI/CD pipelines
- Rotate keys every 90 days
- Monitor API usage for anomalies

### Firebase API Keys
- Firebase API keys are different from typical API keys - they identify your app, not authenticate
- However, unrestricted keys can be abused for quota attacks
- Always add application restrictions

### Certificate Pinning
- If Moonshot rotates their certificates, the app will fail to connect
- Always include a backup pin from a different certificate in the chain
- Monitor for certificate expiration and plan updates
- Consider implementing a fallback mechanism for certificate failures

---

## 🆘 Troubleshooting

### Kimi API Key Issues

**Error: "Invalid API key"**
- Verify the new key is correctly copied to `local.properties`
- Ensure no extra spaces or quotes
- Run `./gradlew clean` before rebuilding

### Firebase Key Issues

**Error: "API key not valid"**
- Verify SHA-1 fingerprint is correct
- Ensure package name matches exactly: `com.pyera.app`
- Wait 5-10 minutes for restrictions to propagate

**Error: "Certificate pinning failure"**
- Verify pins are in correct format: `sha256/BASE64_ENCODED_PIN`
- Ensure primary and backup pins are different
- Test with `openssl` command to verify pins

### Certificate Pinning Issues

**Chat not working after pinning:**
- Check Logcat for: `Certificate pinning failure!`
- Verify the pins match the server's certificate
- Ensure backup pin is from intermediate CA, not the same leaf certificate

---

## 📞 Support Resources

- **Moonshot AI Support**: https://platform.moonshot.cn/docs/support
- **Firebase Support**: https://firebase.google.com/support
- **Google Cloud Console Help**: https://cloud.google.com/support

---

**Complete these actions as soon as possible to secure your application!**
