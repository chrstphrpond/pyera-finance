# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Android
-keep class androidx.appcompat.widget.** { *; }

# Compose
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.material3.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Retrofit
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }
-keep class com.pyera.app.data.local.entity.** { *; }
-keep class com.pyera.app.data.remote.model.** { *; }

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase$Builder { *; }
-keep class * extends androidx.room.RoomOpenHelper$Delegate { *; }
-keep class * extends androidx.room.util.* { *; }
-keep class androidx.room.** { *; }

# Vico
-keep class com.patrykandpatrick.vico.** { *; }

# General
-dontwarn okio.**
-dontwarn javax.annotation.**
