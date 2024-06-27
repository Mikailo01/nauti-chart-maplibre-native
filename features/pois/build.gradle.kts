plugins {
    alias(libs.plugins.nautichart.android.feature)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.bytecause.features.pois"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.presentation)
    implementation(projects.core.util)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.resources)

    implementation(libs.jsoup)

    // Datastore
    implementation(libs.androidx.datastore)

    // Recyclerview
    implementation(libs.androidx.recyclerView)

    // AppCompat
    implementation(libs.androidx.appCompat)

    // Material
    implementation(libs.google.material)
}