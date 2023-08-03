apply(from = "$rootDir/transformer_common.gradle.kts")
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("java-gradle-plugin")
    id("maven-publish")
}

// 使用Gradle发布工件到Maven仓库  https://blog.csdn.net/yingaizhu/article/details/85163062
group = "com.nova.sun"
version = "0.0.2"
val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}
/*publishing{
    repositories {
        maven {
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("$buildDir/repo")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
            pom {

            }
        }
    }
}*/


gradlePlugin{
    plugins {
        register("sword"){
            id = group.toString()
            displayName = "$id.core.plugin"
            description = project.description ?: project.name
            implementationClass = "com.nova.sun.core.plugin"
        }
    }
}

dependencies {
    // ---- plugin ------
    api(project(":plugin:webview"))
    // ---- dependencies ------
    api(project(":transformer:gradle"))
    api(project(":transformer:kotlinx"))
    api(project(":transformer:spi"))
    api(project(":transformer:core"))
    api(project(":transformer:util"))
    api(libs.agp)
    api(gradleApi())
}