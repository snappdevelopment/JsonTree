plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.sebastianneubauer.jsontreesample"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sebastianneubauer.jsontreesample"
        minSdk = 23
        targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // for testing only, don't do this in your app
            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("debug") {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":sample"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.jb.compose.components.resources)
}