plugins {
    alias(libs.plugins.nautichart.android.library)
    alias(libs.plugins.nautichart.android.library.compose)
}

android {
    namespace = "com.bytecause.core.presentation"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.resources)
    implementation(projects.core.util)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.maplibre)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.google.material)
}