
apply(from = "$rootDir/transformer_common.gradle.kts")

fun DependencyHandler.api(dependencyNotation: Any): Dependency? = add("api", dependencyNotation)
fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? = add("implementation", dependencyNotation)
dependencies {
    implementation(project(":transformer:android-api"))
    implementation(project(":transformer:core"))
    implementation(project(":transformer:spi"))
    implementation(project(":transformer:util"))
    implementation(project(":transformer:kotlinx"))
    implementation(project(":transformer:javax"))
}
//import com.nova.build.Configuration

//
//@Suppress("DSL_SCOPE_VIOLATION")
//plugins {
//    id(libs.plugins.android.library.get().pluginId)
//    id(libs.plugins.kotlin.android.get().pluginId)
//}
//
//android {
//    compileSdk = Configuration.compileSdk
//    namespace = "com.nova.plugin.webview"
//    defaultConfig {
//        minSdk = Configuration.minSdk
//        targetSdk = Configuration.targetSdk
//    }
//    sourceSets["main"].java.srcDir("src/main/java")
//    sourceSets["main"].kotlin.srcDir("src/main/kotlin")
//}
//
//dependencies {
////    api(gradleApi())
////    api(libs.kotlin.stdlib)
//    implementation(project(":transformer:core"))
//    implementation(project(":transformer:spi"))
//    implementation(project(":transformer:util"))
//    implementation(project(":transformer:kotlinx"))
//    implementation(project(":transformer:javax"))
//    /*api(gradleApi())
//    api(libs.android.tools.sdklib)
//    api(libs.android.tools.repository)
//    api(libs.agp)*/
//}