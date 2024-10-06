import com.android.build.gradle.LibraryExtension
import com.bytecause.convention.androidTestImplementation
import com.bytecause.convention.configureKotlinAndroid
import com.bytecause.convention.coreLibraryDesugaring
import com.bytecause.convention.libs
import com.bytecause.convention.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 35
            }

            dependencies {
                testImplementation(kotlin("test"))
                androidTestImplementation(libs.findLibrary("androidx-junit").get())
                androidTestImplementation(libs.findLibrary("androidx-espresso").get())

                // desugaring libs
                coreLibraryDesugaring(libs.findLibrary("desugar_jdk_libs").get())
            }
        }
    }
}
