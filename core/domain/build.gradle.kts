plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.core)

    // Testing
    testImplementation(libs.junit)

    // Assertions
    testImplementation(libs.google.truth)
}
