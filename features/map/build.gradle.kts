plugins {
    alias(libs.plugins.nautichart.android.feature)
    alias(libs.plugins.nautichart.android.feature.compose)
    alias(libs.plugins.navigation.safeargs)
    id("com.google.protobuf")
}

android {
    namespace = "com.bytecause.feature.map"
    buildFeatures {
        viewBinding = true
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.4"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(projects.core.util)
    implementation(projects.core.resources)
    implementation(projects.core.data)
    implementation(projects.core.presentation)
    implementation(projects.core.domain)

    // DataStore
    implementation(libs.google.protobuf.javalite)
    implementation(libs.androidx.datastore)

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