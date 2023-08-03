apply(from = "$rootDir/transformer_common.gradle.kts")
fun DependencyHandler.api(dependencyNotation: Any): Dependency? = add("api", dependencyNotation)
fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? = add("implementation", dependencyNotation)
dependencies {
    implementation(project(":transformer:spi"))
    api(libs.asm.core)
    api(libs.asm.commons)
    api(libs.asm.analysis)
    api(libs.asm.tree)
    api(libs.asm.util)
    api(gradleApi())
}
/*
import com.nova.build.Configuration

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
}

android {
    compileSdk = Configuration.compileSdk
    namespace = "com.nova.transform.core"
    defaultConfig {
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
    }
    sourceSets["main"].java.srcDir("src/main/java")
    sourceSets["main"].kotlin.srcDir("src/main/kotlin")
}

dependencies {
    implementation(project(":transformer:spi"))
    api(libs.asm.core)
    api(libs.asm.commons)
    api(libs.asm.analysis)
    api(libs.asm.tree)
    api(libs.asm.util)
    api(gradleApi())
    */
/*api(gradleApi())
    api(libs.android.tools.sdklib)
    api(libs.android.tools.repository)
    api(libs.agp)*//*

}*/
