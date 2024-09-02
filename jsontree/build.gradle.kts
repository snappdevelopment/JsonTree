import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    id("kotlinx-atomicfu")
    alias(libs.plugins.publish)
}

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.addAll("-Xexplicit-api=strict", "-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.toolchain.get()))
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.kotlinx.coroutines.core)

                implementation (libs.kotlinx.serialization.json)

            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)

                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)
                implementation(compose.desktop.currentOs)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
                implementation(compose.desktop.currentOs)
            }
        }
    }
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
}

composeCompiler {
    enableStrongSkippingMode = true
}

apply(plugin = "kotlinx-atomicfu")