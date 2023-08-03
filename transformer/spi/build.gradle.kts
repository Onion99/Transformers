apply(from = "$rootDir/transformer_common.gradle.kts")
fun DependencyHandler.api(dependencyNotation: Any): Dependency? = add("api", dependencyNotation)
fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? = add("implementation", dependencyNotation)