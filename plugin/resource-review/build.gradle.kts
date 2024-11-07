apply(from = "$rootDir/transformer_common.gradle.kts")
fun DependencyHandler.api(dependencyNotation: Any): Dependency? = add("api", dependencyNotation)
fun DependencyHandler.kapt(dependencyNotation: Any): Dependency? = add("kapt", dependencyNotation)
fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? = add("implementation", dependencyNotation)
dependencies {
    compileOnly(project(":transformer:android-api"))
    implementation(project(":transformer:javax"))
    api(project(":transformer:gradle"))
    api(libs.gson)
    api(libs.okio)
    api(libs.android.tools.layoutlib)
    api(libs.android.tools.sdk.common)
}