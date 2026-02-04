# Pyera Finance

A modern, feature-rich personal finance management Android app built with Jetpack Compose and Firebase.

[![Android CI](https://github.com/chrstphrpond/pyera-finance/actions/workflows/android.yml/badge.svg)](https://github.com/chrstphrpond/pyera-finance/actions/workflows/android.yml)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-26%2B-3DDC84.svg?logo=android)](https://developer.android.com)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

## Features

- 📊 **Dashboard** - Overview of your financial health with charts and insights
- 💰 **Transaction Tracking** - Log income and expenses with categories
- 🎯 **Budget Management** - Create and track budgets by category
- 💵 **Savings Goals** - Set and monitor savings targets
- 📈 **Investment Tracking** - Monitor your investment portfolio
- 💳 **Debt Management** - Track and plan debt payoff
- 📅 **Bill Reminders** - Never miss a payment
- 🤖 **AI Financial Assistant** - Get personalized financial advice powered by Gemini AI
- 🔐 **Biometric Authentication** - Secure access with fingerprint/face unlock
- 🔒 **Google Sign-In** - Easy and secure authentication

## Tech Stack

### Core

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3
- **Architecture:** MVVM with Clean Architecture
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)

### Libraries & Frameworks

| Category                 | Technology                                |
| ------------------------ | ----------------------------------------- |
| **Dependency Injection** | Hilt                                      |
| **Database**             | Room                                      |
| **Backend**              | Firebase (Auth, Firestore, Analytics)     |
| **Networking**           | Retrofit + OkHttp                         |
| **Navigation**           | Jetpack Navigation Compose                |
| **Charts**               | Vico                                      |
| **AI**                   | Google Gemini AI                          |
| **OCR**                  | ML Kit Text Recognition                   |
| **Security**             | EncryptedSharedPreferences, Biometric API |

## Project Structure

```
pyera-finance/
├── app/
│   └── src/main/java/com/pyera/app/
│       ├── data/           # Data layer (repositories, models, DAOs)
│       ├── di/             # Dependency injection modules
│       ├── domain/         # Business logic & use cases
│       └── ui/
│           ├── analysis/       # Financial analysis screens
│           ├── auth/           # Login & registration
│           ├── bills/          # Bill management
│           ├── budget/         # Budget tracking
│           ├── chat/           # AI assistant
│           ├── components/     # Reusable UI components
│           ├── dashboard/      # Home dashboard
│           ├── debt/           # Debt tracking
│           ├── investments/    # Investment portfolio
│           ├── navigation/     # App navigation
│           ├── profile/        # User profile
│           ├── savings/        # Savings goals
│           ├── theme/          # App theming
│           └── transaction/    # Transaction management
├── functions/              # Firebase Cloud Functions
├── docs/                   # Documentation
├── firebase.json           # Firebase configuration
├── firestore.rules         # Firestore security rules
└── firestore.indexes.json  # Firestore indexes
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK 34
- Firebase project with Firestore and Authentication enabled

### Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/chrstphrpond/pyera-finance.git
   cd pyera-finance
   ```

2. **Set up Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication (Email/Password and Google Sign-In)
   - Enable Cloud Firestore
   - Download `google-services.json` and place it in `app/`

3. **Configure API Keys**

   Create or update `local.properties`:

   ```properties
   KIMI_API_KEY=your_api_key_here
   ```

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

### Running the App

```bash
./gradlew installDebug
```

Or open the project in Android Studio and click **Run**.

## Security

- Sensitive credentials are stored using `EncryptedSharedPreferences`
- API keys are loaded from `local.properties` (not committed to VCS)
- Firebase service account keys should be stored securely outside the repository
- Biometric authentication is available for app access

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

Please note that this project is released with a [Code of Conduct](CODE_OF_CONDUCT.md). By participating in this project you agree to abide by its terms.

## License

This project is proprietary software. All rights reserved.

## Author

**Christopher Pond** - [@chrstphrpond](https://github.com/chrstphrpond)

---

<p align="center">Made with ❤️ in the Philippines</p>
