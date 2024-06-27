import com.bytecause.convention.implementation

plugins {
    alias(libs.plugins.nautichart.android.feature)
    alias(libs.plugins.nautichart.android.hilt)
}

android {
    namespace = "com.bytecause.features.download_tiles"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.resources)
    implementation(projects.core.util)
    implementation(projects.core.presentation)
    implementation(projects.core.domain)

    implementation(libs.osmdroid.mapsforge)
    implementation(libs.osmdroid)

    implementation(libs.maplibre)

    implementation(libs.google.material)
}