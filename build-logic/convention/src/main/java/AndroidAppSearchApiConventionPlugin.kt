import com.bytecause.convention.implementation
import com.bytecause.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidAppSearchApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.kapt")
            }

            dependencies {
                implementation(libs.findLibrary("appSearchApi").get())
                implementation(libs.findLibrary("appSearchLocalStorage").get())
                "kapt"(libs.findLibrary("appSearchCompiler").get())
            }
        }
    }
}