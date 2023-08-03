apply(from = "$rootDir/transformer_common.gradle.kts")
fun DependencyHandler.api(dependencyNotation: Any): Dependency? = add("api", dependencyNotation)
fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? = add("implementation", dependencyNotation)
dependencies {
    implementation(project(":transformer:spi"))
    implementation(project(":transformer:kotlinx"))
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")
}
/*
import com.nova.build.Configuration

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
}

android {
    namespace = "com.nova.transformer.util"
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
