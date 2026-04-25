import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.publish)
}

kotlin {
    android {
        namespace = "com.sebastianneubauer.jsontree"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        aarMetadata {
            minCompileSdk = libs.versions.android.minSdk.get().toInt()
        }

        androidResources.enable = true
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js {
        browser()
        useEsModules()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.addAll("-Xexplicit-api=strict", "-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.toolchain.get()))
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jb.compose.runtime)
            implementation(libs.jb.compose.foundation)
            implementation(libs.jb.compose.material3)
            implementation(libs.jb.compose.ui.tooling.preview)
            implementation(libs.jb.compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlindiff)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(libs.jb.compose.ui.test)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(compose.desktop.currentOs)
        }

        jvmTest.dependencies {
            implementation(libs.jb.compose.ui.test.junit4)
            implementation(compose.desktop.currentOs)
        }
    }
}