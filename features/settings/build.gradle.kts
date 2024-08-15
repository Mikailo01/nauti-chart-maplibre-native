plugins {
    alias(libs.plugins.nautichart.android.feature)
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
}