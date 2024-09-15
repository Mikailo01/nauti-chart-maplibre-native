plugins {
    alias(libs.plugins.nautichart.android.feature)
    alias(libs.plugins.nautichart.android.feature.compose)
}

android {
    namespace = "com.bytecause.features.first_run"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.util)
    implementation(projects.core.presentation)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.resources)

    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.google.material)
    implementation(libs.maplibre)
    implementation(libs.androidx.compose.material3)
}