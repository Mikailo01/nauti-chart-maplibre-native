import com.android.build.api.dsl.ApplicationExtension
import com.bytecause.convention.configureKotlinAndroid
import com.bytecause.convention.coreLibraryDesugaring
import com.bytecause.convention.implementation
import com.bytecause.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 35
            }

            dependencies {
                implementation(libs.findLibrary("timber").get())
                // desugaring libs
                coreLibraryDesugaring(libs.findLibrary("desugar_jdk_libs").get())
            }
        }
    }
}