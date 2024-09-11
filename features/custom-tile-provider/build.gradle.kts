import com.bytecause.convention.implementation

plugins {
    alias(libs.plugins.nautichart.android.feature.compose)
}

android {
    namespace = "com.bytecause.nautichart.features.custom_tile_provider"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.util)
    implementation(projects.core.presentation)
    implementation(projects.core.data)
    implementation(projects.core.resources)
    implementation(projects.core.domain)

    implementation(libs.simplestorage.storage)

    // loading animations
    implementation(libs.compose.loadingAnimations)

    // Protobuf
    implementation(libs.google.protobuf.javalite)
}