import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

group = "com.sebastianneubauer.jsontreesample"
version = "1.0"

kotlin {
    android {
        namespace = "com.sebastianneubauer.jsontreesample.shared"
        compileSdk = 36
        minSdk = 23

        androidResources.enable = true
    }

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "Sample"
            isStatic = true
        }
    }

    js {
        outputModuleName = "jsontree"
        browser()
        binaries.executable()
        useEsModules()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "jsontree"
        browser()
        binaries.executable()
    }

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
        commonMain.dependencies {
            implementation(libs.jb.compose.runtime)
            implementation(libs.jb.compose.foundation)
            implementation(libs.jb.compose.material3)
            implementation(libs.jb.compose.ui.tooling.preview)
            implementation(libs.jb.compose.components.resources)
            implementation(libs.kotlinx.serialization.json)
            implementation(project(":jsontree"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compilerOptions.options.freeCompilerArgs.add("-Xexport-kdoc")
    }

    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "com.sebastianneubauer.jsontreesample.MainKt"
        buildTypes.release {
            proguard {
                configurationFiles.from("compose-desktop.pro")
            }
        }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.sebastianneubauer.jsontreesample"
            packageVersion = "1.0.0"
        }
    }
}
