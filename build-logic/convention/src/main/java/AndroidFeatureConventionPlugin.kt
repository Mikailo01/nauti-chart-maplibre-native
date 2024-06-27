import com.bytecause.convention.androidTestImplementation
import com.bytecause.convention.implementation
import com.bytecause.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("nautichart.android.library")
                apply("nautichart.android.hilt")
            }

            dependencies {
                // add dependencies for all features
                implementation(libs.findLibrary("androidx-navigation-fragmentKtx").get())

                // Testing
                androidTestImplementation(libs.findLibrary("junit").get())
                androidTestImplementation(libs.findLibrary("androidx-junit").get())
            }
        }
    }
}