apply(from = "$rootDir/transformer_common.gradle.kts")
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("java-gradle-plugin")
    id("maven-publish")
}

// 使用Gradle发布工件到Maven仓库  https://blog.csdn.net/yingaizhu/article/details/85163062
//group = "com.nova.sun"
//version = "0.0.2"
//val sourcesJar by tasks.registering(Jar::class) {
//    classifier = "sources"
//    from(sourceSets.main.get().allSource)
//}
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
//sourceSets.getByName("main"){
//    resources.srcDir("resources")
//}

gradlePlugin{
    plugins {
        create("onion"){
            id = "com.onion.plugin"
            description = project.description ?: project.name
            // 实现这个插件的类的路径
            implementationClass = "com.nova.plugin.main.CorePlugin"
        }
    }
}

dependencies {
    // ---- plugin ------
    api(project(":plugin:webview"))
    api(project(":plugin:resource-review"))
    // ---- dependencies ------
    api(project(":transformer:gradle"))
    api(project(":transformer:gradle-api-v74"))
    api(project(":transformer:kotlinx"))
    api(project(":transformer:spi"))
    api(project(":transformer:core-asm"))
    api(project(":transformer:util"))
    api(libs.agp)
    api(libs.gson)
    api("commons-codec:commons-codec:1.15")
    api("com.squareup.okio:okio:3.8.0")
    api(gradleApi())
}