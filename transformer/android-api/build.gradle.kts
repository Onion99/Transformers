apply(from = "$rootDir/transformer_common.gradle.kts")

/*
import com.nova.build.Configuration

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
}

android {
    namespace = "com.nova.transformer.api"
    compileSdk = Configuration.compileSdk
    defaultConfig {
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
    }
    sourceSets["main"].java.srcDir("src/main/java")
    sourceSets["main"].kotlin.srcDir("src/main/kotlin")
}

*/

