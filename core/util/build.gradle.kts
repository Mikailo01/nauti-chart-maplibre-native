plugins {
    alias(libs.plugins.nautichart.android.library)
    alias(libs.plugins.nautichart.android.library.compose)
}

android {
    namespace = "com.bytecause.core.util"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.resources)

    implementation(libs.maplibre)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.recyclerView)
    implementation(libs.simplestorage.storage)
}