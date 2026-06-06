import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

group = "com.sebastianneubauer.jsontreesample"
version = "1.0"

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
}

kotlin {
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
