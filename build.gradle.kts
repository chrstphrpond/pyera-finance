@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.hiltAndroid) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.gmsGoogleServices) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

// KtLint configuration for all subprojects
allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.2.1")
        android.set(true)
        outputToConsole.set(true)
        outputColorName.set("RED")
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
        
        // KtLint settings
        additionalEditorconfig.set(
            mapOf(
                "indent_size" to "4",
                "indent_style" to "space",
                "max_line_length" to "120",
                "insert_final_newline" to "true",
                "ktlint_standard_trailing-comma-on-call-site" to "enabled",
                "ktlint_standard_trailing-comma-on-declaration-site" to "enabled",
                "ktlint_standard_multiline-expression-wrapping" to "enabled",
                "ktlint_standard_string-template-indent" to "enabled",
                "ktlint_standard_no-empty-first-line-in-class-body" to "disabled",
            )
        )
    }
}

// Detekt configuration
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("$rootDir/detekt-config.yml"))
        buildUponDefaultConfig = false
        allRules = false
        autoCorrect = true

        reports {
            html.required.set(true)
            xml.required.set(true)
            sarif.required.set(true)
            md.required.set(false)
        }
    }

    dependencies {
        add("detektPlugins", "io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
        add("detektPlugins", "io.gitlab.arturbosch.detekt:detekt-rules-libraries:1.23.6")
    }
}

// Task to run all code quality checks
tasks.register<GradleBuild>("codeQuality") {
    description = "Runs all code quality checks (Detekt + KtLint + Lint)"
    group = "verification"
    tasks = listOf("detekt", "ktlintCheck", "lint")
}
true // Needed to make the script return true (kotlin-dsl requirement sometimes)
