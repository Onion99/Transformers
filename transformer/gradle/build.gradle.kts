apply(from = "$rootDir/transformer_common.gradle.kts")
fun DependencyHandler.api(dependencyNotation: Any): Dependency? = add("api", dependencyNotation)
dependencies {
    api(gradleApi())
    api(libs.android.tools.sdklib)
    api(libs.android.tools.repository)
    api(libs.agp)
}
/*
import com.nova.build.Configuration

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
}

android {
    namespace = "com.nova.transformer.gradle.api"
    compileSdk = Configuration.compileSdk
    defaultConfig {
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
    }
    sourceSets["main"].java.srcDir("src/main/java")
    sourceSets["main"].kotlin.srcDir("src/main/kotlin")
}

dependencies {
    api(gradleApi())
    api(libs.android.tools.sdklib)
    api(libs.android.tools.repository)
    api(libs.agp)
}*/
