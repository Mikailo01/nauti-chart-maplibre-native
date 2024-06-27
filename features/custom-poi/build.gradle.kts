import com.bytecause.convention.implementation

plugins {
    alias(libs.plugins.nautichart.android.feature)
    id("com.google.protobuf")
}

android {
    namespace = "com.bytecause.features.custom_poi"
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
    implementation(projects.core.data)
    implementation(projects.core.resources)
    implementation(projects.core.presentation)
    implementation(projects.core.domain)

    implementation(libs.maplibre)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.recyclerView)
    implementation(libs.google.material)
    implementation(libs.androidx.navigation.fragmentKtx)

    // DataStore
    implementation(libs.androidx.datastore)
    implementation(libs.google.protobuf.javalite)
}