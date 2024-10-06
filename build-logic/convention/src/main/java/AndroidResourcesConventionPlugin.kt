import com.android.build.gradle.LibraryExtension
import com.bytecause.convention.configureKotlinAndroid
import com.bytecause.convention.coreLibraryDesugaring
import com.bytecause.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidResourcesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 35
            }

            dependencies {
                // desugaring libs
                coreLibraryDesugaring(libs.findLibrary("desugar_jdk_libs").get())
            }
        }
    }
}