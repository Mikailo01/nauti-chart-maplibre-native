plugins {
    alias(libs.plugins.nautichart.android.feature)
    alias(libs.plugins.nautichart.android.feature.compose)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.bytecause.feature.map"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.util)
    implementation(projects.core.resources)
    implementation(projects.core.data)
    implementation(projects.core.presentation)
    implementation(projects.core.domain)

    // RecyclerView
    implementation(libs.androidx.recyclerView)

    // MapLibre SDK
    implementation(libs.maplibre)
    implementation(libs.maplibre.annotation)

    // Material
    implementation(libs.google.material)

    // Coil
    implementation(libs.coil)

    // Jsoup
    implementation(libs.jsoup)

}