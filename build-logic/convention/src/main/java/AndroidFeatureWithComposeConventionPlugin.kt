import com.android.build.api.dsl.LibraryExtension
import com.bytecause.convention.configureAndroidCompose
import com.bytecause.convention.coreLibraryDesugaring
import com.bytecause.convention.implementation
import com.bytecause.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidFeatureWithComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("nautichart.android.library")
                apply("nautichart.android.hilt")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            val extension = extensions.getByType<LibraryExtension>()
            configureAndroidCompose(extension)

            dependencies {
                // add dependencies for all features
                implementation(libs.findLibrary("androidx-navigation-fragmentKtx").get())
                implementation(libs.findLibrary("androidx-compose-lifecycle").get())
                implementation(libs.findLibrary("timber").get())

                // desugaring libs
                coreLibraryDesugaring(libs.findLibrary("desugar_jdk_libs").get())
            }
        }
    }
}