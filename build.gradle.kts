import io.gitlab.arturbosch.detekt.Detekt

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
    alias(libs.plugins.detekt)
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
}

apiValidation {
    ignoredProjects.addAll(listOf("shared", "desktopApp", "webApp"))
}

val projectSource = file(projectDir)
val configFiles = files("$rootDir/detekt/config.yml")
val baselineFile = File("$rootDir/detekt/baseline.xml")
val kotlinFiles = "**/*.kt"
val sharedModuleFiles = "**/shared/**"
val desktopAppModuleFiles = "**/desktopApp/**"
val webAppModuleFiles = "**/webApp/**"
val resourceFiles = "**/resources/**"
val buildFiles = "**/build/**"
val testFiles = "**/commonTest/**"

tasks.register<Detekt>("detektAll") {
    val autoFix = project.hasProperty("detektAutoFix")

    description = "Custom DETEKT task for all modules"
    parallel = true
    ignoreFailures = false
    autoCorrect = autoFix
    buildUponDefaultConfig = true
    setSource(projectSource)
    config.setFrom(configFiles)
    baseline = baselineFile
    reports {
        html.required = true
        xml.required = false
        txt.required = false
    }
}

tasks.register<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektGenerateBaseline") {
    description = "Custom DETEKT task to build baseline for all modules"
    parallel = true
    ignoreFailures = false
    buildUponDefaultConfig = true
    setSource(projectSource)
    baseline.set(baselineFile)
    config.setFrom(configFiles)
    include(kotlinFiles)
    exclude(sharedModuleFiles, desktopAppModuleFiles, webAppModuleFiles, resourceFiles, buildFiles, testFiles)
}

tasks.withType<Detekt>().configureEach {
    include(kotlinFiles)
    exclude(sharedModuleFiles, desktopAppModuleFiles, webAppModuleFiles, resourceFiles, buildFiles, testFiles)
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}
