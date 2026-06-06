pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "JsonTree"
include(":jsontree")
include(":shared")
include(":androidApp")
include(":desktopApp")
include(":webApp")
