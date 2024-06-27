import com.android.build.api.dsl.LibraryExtension
import com.bytecause.convention.configureAndroidCompose
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
            }

            val extension = extensions.getByType<LibraryExtension>()
            configureAndroidCompose(extension)

            dependencies {
                // add dependencies for all features
                implementation(libs.findLibrary("androidx-navigation-fragmentKtx").get())
            }
        }
    }
}