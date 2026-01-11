import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.publish)
}

kotlin {
    jvm()

    android {
        namespace = "com.sebastianneubauer.jsontree"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        aarMetadata {
            minCompileSdk = libs.versions.android.minSdk.get().toInt()
        }

        androidResources {
            enable = true
        }

        withHostTest {
            isIncludeAndroidResources = true
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
        }

        // fixes lint error in release builds for compose 1.9.3
        lint {
            disable.add("NullSafeMutableLiveData")
        }
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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.compose.ui.test.android)
            implementation(libs.androidx.compose.ui.test.manifest)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(compose.desktop.currentOs)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.uiTestJUnit4)
            implementation(compose.desktop.currentOs)
        }

//        getByName("androidDeviceTest") {
//            dependencies {
//                implementation(libs.kotlinx.coroutines.test)
////                testImplementation(libs.androidx.compose.ui.test.android)
////                implementation(libs.androidx.compose.ui.test.manifest)
//            }
//        }
    }
}

//dependencies {
//    implementation(libs.kotlinx.coroutines.test)
//    androidTestImplementation(libs.androidx.compose.ui.test.android)
//    debugImplementation(libs.androidx.compose.ui.test.manifest)
//}
//
//android {
//    namespace = "com.sebastianneubauer.jsontree"
//    compileSdk = libs.versions.android.compileSdk.get().toInt()
//    buildFeatures {
//        buildConfig = false
//        compose = true
//    }
//    defaultConfig {
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        minSdk = libs.versions.android.minSdk.get().toInt()
//        aarMetadata {
//            minCompileSdk = libs.versions.android.minSdk.get().toInt()
//        }
//    }
//
//    // fixes lint error in release builds for compose 1.9.3
//    lint {
//        disable.add("NullSafeMutableLiveData")
//    }
//}