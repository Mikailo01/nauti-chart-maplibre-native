import com.bytecause.convention.implementation

plugins {
    alias(libs.plugins.nautichart.android.library)
    alias(libs.plugins.nautichart.android.hilt)
    id("com.google.protobuf")
}

android {
    namespace = "com.bytecause.core.data"
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
    implementation(projects.core.domain)
    implementation(projects.core.resources)

    implementation(libs.androidx.room)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.activity.ktx)

    implementation(libs.hilt.work)

    // Lifecycle service
    implementation(libs.androidx.lifecycle.lifecycleService)

    // Gson
    implementation(libs.gson)

    // Google services location
    implementation(libs.gms.playServicesLocation)

    // Moshi
    implementation(libs.moshi)

    // MapLibre SDK
    implementation(libs.maplibre)

    // Retrofit
    implementation(libs.retrofit)

    // DataStore
    implementation(libs.androidx.datastore)
    implementation(libs.google.protobuf.javalite)
}