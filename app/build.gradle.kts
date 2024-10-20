import com.bytecause.convention.implementation

plugins {
    alias(libs.plugins.nautichart.android.application)
    alias(libs.plugins.nautichart.android.hilt)
    alias(libs.plugins.nautichart.android.application.compose)
}

android {
    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "mozilla/public-suffix-list.txt"
            )
        }
    }

    defaultConfig {
        versionCode = rootProject.extra["appVersionCode"] as Int
        versionName = rootProject.extra["appVersionName"] as String
    }

    namespace = "com.bytecause.nautichart"

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }
}

dependencies {
    // Features
    implementation(projects.features.settings)
    implementation(projects.features.pois)
    implementation(projects.features.search)
    implementation(projects.features.customPoi)
    implementation(projects.features.firstRun)
    implementation(projects.features.customTileProvider)
    implementation(projects.features.map)

    // Core dependencies
    implementation(projects.core.util)
    implementation(projects.core.domain)
    implementation(projects.core.resources)
    implementation(projects.core.data)
    implementation(projects.core.presentation)

    // WorkManager
    implementation(libs.androidx.workManager)

    // Google services location
    implementation(libs.gms.playServicesLocation)

    implementation(libs.hilt.work)

    // Lifecycle service
    implementation(libs.androidx.lifecycle.lifecycleService)

    // Navigation
    implementation(libs.androidx.navigation.uiKtx)
    implementation(libs.androidx.navigation.fragmentKtx)

    // MapLibre SDK
    implementation(libs.maplibre)

    // Hilt
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    // SplashScreen
    implementation(libs.androidx.splashScreen)

    // DataStore
    implementation(libs.google.protobuf.javalite)

    // Testing
    testImplementation(libs.androidx.junit)

}