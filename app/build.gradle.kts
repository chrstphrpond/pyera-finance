plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.gmsGoogleServices)
}

android {
    namespace = "com.pyera.app"
    compileSdk = 34

    val certPin1 = project.findProperty("PYERA_CERT_PIN_1")?.toString()?.trim().orEmpty()
    val certPin2 = project.findProperty("PYERA_CERT_PIN_2")?.toString()?.trim().orEmpty()
    val geminiApiKey = project.findProperty("GEMINI_API_KEY")?.toString()?.trim().orEmpty()
    val enableCertPinning = certPin1.isNotBlank() || certPin2.isNotBlank()

    fun buildConfigString(value: String) = "\"${value.replace("\"", "\\\"")}\""

    defaultConfig {
        applicationId = "com.pyera.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // APK Size Optimization: Only include English resources
        // This filters out unused language resources from dependencies
        resConfigs("en")

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "ENABLE_CERT_PINNING", enableCertPinning.toString())
            buildConfigField("String", "CERT_PIN_1", buildConfigString(certPin1))
            buildConfigField("String", "CERT_PIN_2", buildConfigString(certPin2))
            buildConfigField("String", "GEMINI_API_KEY", buildConfigString(geminiApiKey))
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("boolean", "ENABLE_CERT_PINNING", enableCertPinning.toString())
            buildConfigField("String", "CERT_PIN_1", buildConfigString(certPin1))
            buildConfigField("String", "CERT_PIN_2", buildConfigString(certPin2))
            buildConfigField("String", "GEMINI_API_KEY", buildConfigString(geminiApiKey))
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs +=
            listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    // ABI splits for smaller APKs
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = false
        }
    }

    // Resource optimization
    packaging {
        resources {
            excludes +=
                listOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.txt",
                    "META-INF/NOTICE",
                    "META-INF/NOTICE.txt",
                )
        }
    }

    // Lint configuration
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// KSP configuration - disable incremental processing to avoid issues
ksp {
    arg("room.incremental", "false")
    arg("room.expandProjection", "true")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation("androidx.compose.material:material")
    implementation(libs.androidx.material3)
    implementation(libs.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)

    // ML Kit
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    // Gemini AI
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Biometric
    implementation(libs.androidx.biometric)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Security (EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // SQLCipher for database encryption
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // WorkManager Testing
    testImplementation("androidx.work:work-testing:2.9.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-android:1.13.8")
    testImplementation("androidx.test:core:1.5.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Charts (Vico)
    implementation("com.patrykandpatrick.vico:compose:1.14.0")
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")
    implementation("com.patrykandpatrick.vico:core:1.14.0")

    // Accompanist (pull-to-refresh, etc.)
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // For Excel export
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    // For PDF export
    implementation("com.itextpdf:itext7-core:8.0.2")
}
