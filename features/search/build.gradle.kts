plugins {
    alias(libs.plugins.nautichart.android.feature)
    alias(libs.plugins.kapt)
    alias(libs.plugins.navigation.safeargs)
    id("com.google.protobuf")
}

android {
    android.buildFeatures.buildConfig = true

    namespace = "com.bytecause.features.search"
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        buildConfigField(
            "String",
            "APP_VERSION",
            "\"${rootProject.extra["appVersionName"] as String}\""
        )
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

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(projects.core.util)
    implementation(projects.core.presentation)
    implementation(projects.core.resources)
    implementation(projects.core.data)
    implementation(projects.core.domain)

    // Viewpager2
    implementation(libs.androidx.viewpager2)

    // Material
    implementation(libs.google.material)

    // MapLibre SDK
    implementation(libs.maplibre)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)

    // Datastore
    implementation(libs.androidx.datastore)
    implementation(libs.google.protobuf.javalite)

    // AppSearch Api
    implementation(libs.appSearchApi)
    implementation(libs.appSearchLocalStorage)
    kapt(libs.appSearchCompiler)
}