package com.bytecause.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            implementation(platform(bom))
            androidTestImplementation(platform(bom))
            implementation(libs.findLibrary("androidx-compose-activity").get())
            implementation(libs.findLibrary("androidx-compose-ui").get())
            implementation(libs.findLibrary("androidx.compose.material3").get())
            debugImplementation(libs.findLibrary("androidx-compose-ui-tooling").get())
            implementation(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            implementation(libs.findLibrary("navigation-hilt-compose-navigation").get())

            androidTestImplementation(libs.findLibrary("junit").get())
            androidTestImplementation(libs.findLibrary("androidx-junit").get())
        }
    }
}
