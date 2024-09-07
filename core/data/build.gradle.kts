import com.bytecause.convention.implementation

plugins {
    alias(libs.plugins.nautichart.android.library)
    alias(libs.plugins.nautichart.android.hilt)
    alias(libs.plugins.nautichart.android.appsearchapi)
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

    // WorkManager
    implementation(libs.androidx.workManager)

    // Lifecycle service
    implementation(libs.androidx.lifecycle.lifecycleService)

    // Gson
    implementation(libs.gson)

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