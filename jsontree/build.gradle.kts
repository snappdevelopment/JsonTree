import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.atomicfu)
    alias(libs.plugins.publish)
}

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }
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
            // needs to be added as a workaround not get atomicfus code stripped
            implementation(libs.atomicfu)
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

dependencies {
    implementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.compose.ui.test.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

android {
    namespace = "com.sebastianneubauer.jsontree"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildFeatures {
        buildConfig = false
        compose = true
    }
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        minSdk = libs.versions.android.minSdk.get().toInt()
        aarMetadata {
            minCompileSdk = libs.versions.android.minSdk.get().toInt()
        }
    }

    // fixes lint error in release builds for compose 1.9.3
    lint {
        disable.add("NullSafeMutableLiveData")
    }
}