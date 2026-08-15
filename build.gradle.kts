buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinJvm).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.publish).apply(false)
    alias(libs.plugins.api.validator)
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
}

apiValidation {
    ignoredProjects.addAll(listOf("shared", "desktopApp", "webApp"))
}
