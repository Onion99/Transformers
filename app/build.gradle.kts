/*
 * Designed and developed by 2022 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.nova.build.Configuration
import java.time.LocalDateTime

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.application.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.kapt.get().pluginId)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
//    id("com.onion.plugin")
}
android {

    @Suppress("UnstableApiUsage")
    packagingOptions {
        jniLibs.keepDebugSymbols += "*/armeabi-v7a/libdu.so"
        jniLibs.keepDebugSymbols += "*/arm64-v8a/libdu.so"
    }

    compileSdk = Configuration.compileSdk
    namespace = Configuration.nameSpace
    @Suppress("UnstableApiUsage")
    defaultConfig {
        //applicationId = Configuration.debugNameSpace
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
        versionCode = Configuration.versionCode
        versionName = Configuration.versionName
        vectorDrawables.useSupportLibrary = true
        ndk {
            abiFilters.clear()
            //noinspection ChromeOsAbiSupport
            abiFilters += "armeabi-v7a"
            //noinspection ChromeOsAbiSupport
            abiFilters += "arm64-v8a"
        }
    }

    @Suppress("UnstableApiUsage")
    bundle {
        language.enableSplit = false
    }

    @Suppress("UnstableApiUsage")
    flavorDimensions += "normal"

    @Suppress("UnstableApiUsage")
    signingConfigs {
        getByName("debug") {
            storeFile = file("${project.projectDir.absolutePath}/sign/debug.jks")
            storePassword = Configuration.debugSignPassWord
            keyAlias = Configuration.debugSignAlias
            keyPassword = Configuration.debugSignPassWord
        }
        create("release") {
            storeFile = file("${project.projectDir.absolutePath}/sign/release.jks")
            storePassword = Configuration.releaseSignPassWord
            keyAlias = Configuration.releaseSignAlias
            keyPassword = Configuration.releaseSignPassWord
        }
    }

    @Suppress("UnstableApiUsage")
    productFlavors {
        create("normal") {
            applicationId = Configuration.debugApplicationId
            manifestPlaceholders["app_name"] = "${Configuration.app.capitalize()}-DEV"
            signingConfig = signingConfigs.getByName("debug") }
        create("package") {
            manifestPlaceholders["app_name"] = Configuration.app.capitalize()
            applicationId = Configuration.releaseApplicationId
            signingConfig = signingConfigs.getByName("release")
        }
    }

    @Suppress("UnstableApiUsage")
    buildTypes {
        getByName("debug") {
            signingConfig = null
            isDebuggable = true
            setProperty("archivesBaseName", "${Configuration.app.toUpperCase()}-DEV-v${Configuration.versionCode}(${Configuration.versionName})")
        }

        getByName("release") {
            signingConfig = null
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            setProperty("archivesBaseName", "${Configuration.app.toUpperCase()}-v${Configuration.versionCode}(${Configuration.versionName})")
        }
    }

    androidComponents {
        beforeVariants {
            if(it.productFlavors.contains(Pair("normal", "package")) && it.buildType == "debug") {
                it.enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlin {
        sourceSets.configureEach {
            kotlin.srcDir("$buildDir/generated/ksp/$name/kotlin/")
        }
    }

    @Suppress("UnstableApiUsage")
    lint {
        abortOnError = false
    }

    @Suppress("UnstableApiUsage")
    buildFeatures {
        // viewBinding = true
        // Determines whether to generate a BuildConfig class.
        // buildConfig = true
        // Determines whether to support Data Binding.
        // Note that the dataBinding.enabled property is now deprecated.
        // dataBinding = false
        // Determines whether to generate binder classes for your AIDL files.
        // aidl = true
        // Determines whether to support RenderScript.
        // renderScript = true
        // Determines whether to support injecting custom variables into the module’s R class.
        // resValues = true
        // Determines whether to support shader AOT compilation.
        // shaders = true
    }
}


dependencies {
    // androidx
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.timber)
    implementation(libs.material)

}
apply(from = "$rootDir/exclude_other_version.gradle")