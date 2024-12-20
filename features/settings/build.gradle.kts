import com.bytecause.convention.implementation

plugins {
    alias(libs.plugins.nautichart.android.feature.compose)
}

android {
    namespace = "com.bytecause.features.settings"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.util)
    implementation(projects.core.presentation)
    implementation(projects.core.resources)
    implementation(projects.core.data)
    implementation(projects.core.domain)

    // Recyclerview
    implementation(libs.androidx.recyclerView)

    // AppCompat
    implementation(libs.androidx.appCompat)

    // Lifecycle service
    implementation(libs.androidx.lifecycle.lifecycleService)

    // Material
    implementation(libs.google.material)
}