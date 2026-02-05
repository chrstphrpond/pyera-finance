package com.pyera.app.data.security

import android.content.Context
import android.os.Build
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityChecker @Inject constructor(
    private val context: Context
) {
    
    fun isDeviceSecure(): Boolean {
        return !isRooted() && !isEmulator()
    }
    
    fun getSecurityIssues(): List<String> {
        val issues = mutableListOf<String>()
        if (isRooted()) issues.add("Device appears to be rooted")
        if (isEmulator()) issues.add("App is running on emulator")
        return issues
    }
    
    private fun isRooted(): Boolean {
        // Check for superuser files
        val testPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        if (testPaths.any { File(it).exists() }) return true
        
        // Check for busybox
        if (File("/system/xbin/busybox").exists()) return true
        
        // Check for test-keys build tag
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true
        
        // Try to execute su command
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT?.startsWith("generic") == true ||
                Build.FINGERPRINT?.startsWith("unknown") == true ||
                Build.MODEL?.contains("google_sdk") == true ||
                Build.MODEL?.contains("Emulator") == true ||
                Build.MODEL?.contains("Android SDK built for x86") == true ||
                Build.BOARD?.lowercase()?.contains("nox") == true ||
                Build.BOOTLOADER?.lowercase()?.contains("nox") == true ||
                Build.HARDWARE == "goldfish" ||
                Build.HARDWARE == "ranchu" ||
                Build.HARDWARE?.lowercase()?.contains("nox") == true ||
                Build.PRODUCT?.contains("sdk") == true ||
                Build.PRODUCT?.contains("nox") == true)
    }
}
