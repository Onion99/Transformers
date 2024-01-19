apply(from = "$rootDir/transformer_common.gradle.kts")
fun DependencyHandler.api(dependencyNotation: Any): Dependency? = add("api", dependencyNotation)
fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? = add("implementation", dependencyNotation)
fun DependencyHandler.kapt(dependencyNotation: Any): Dependency? = add("kapt", dependencyNotation)
dependencies {
    api(libs.javassist)
    api(libs.autoService)
    kapt(libs.autoService)
    api(project(":transformer:spi"))
    api(project(":transformer:kotlinx"))
    api(project(":transformer:util"))
}
/*
import com.nova.build.Configuration

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
}

android {
    namespace = "com.nova.transformer.kotlinx"
    compileSdk = Configuration.compileSdk
    defaultConfig {
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
    }
    sourceSets["main"].java.srcDir("src/main/java")
    sourceSets["main"].kotlin.srcDir("src/main/kotlin")
}

dependencies {
    */
/*api(gradleApi())
    api(libs.android.tools.sdklib)
    api(libs.android.tools.repository)
    api(libs.agp)*//*

}*/
