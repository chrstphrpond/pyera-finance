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
-keep class com.google.android.gms.** { *; }

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

# Kimi API models - Keep for Gson serialization
-keep class com.pyera.app.data.repository.KimiRequest { *; }
-keep class com.pyera.app.data.repository.KimiResponse { *; }
-keep class com.pyera.app.data.repository.KimiMessage { *; }
-keep class com.pyera.app.data.repository.KimiChoice { *; }
-keep class com.pyera.app.data.repository.KimiError { *; }

# Room entities - Keep all entity classes
-keep class com.pyera.app.data.local.entity.* { *; }

# Keep constructors for entities (used by Room)
-keepclassmembers class com.pyera.app.data.local.entity.* {
    <init>(...);
}

# Serializable/Parcelable - Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Data classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Hilt
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManagerHolder { *; }

# API classes for Retrofit
-keep class com.pyera.app.data.remote.api.** { *; }

# Remove logging in release builds - Strip all Log calls
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# SecurityChecker - Keep for root detection
-keep class com.pyera.app.security.SecurityChecker { *; }
-keep class com.pyera.app.security.SecurePassphraseManager { *; }

# ValidationUtils - Keep
-keep class com.pyera.app.util.ValidationUtils { *; }
