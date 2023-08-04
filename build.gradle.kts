// KTS教程: https://github.com/gradle/kotlin-dsl-samples/blob/master/samples/maven-publish/build.gradle.kts
import  com.nova.build.Configuration

plugins {
    //alias(libs.plugins.spotless)
    id("maven-publish")
    id("java")
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

buildscript {
    repositories {
        mavenLocal()
        maven(uri("https://repo1.maven.org/maven2/"))
        maven(uri("https://maven.aliyun.com/repository/public"))
        maven(uri("https://maven.aliyun.com/repository/google"))
        maven(uri("https://jitpack.io"))
        google()
        mavenCentral()
        jcenter()
        maven(uri("https://oss.sonatype.org/content/repositories/snapshots"))
    }

    dependencies {
        classpath(libs.agp)
        classpath(libs.kotlin.gradlePlugin)
        classpath("com.nova.sun:main:0.0.2")
    }
}

apply(from = "$rootDir/exclude_other_version.gradle")



allprojects {
    val noTransformerProject = arrayOf("app", "Transformers","transformer")
    if (!noTransformerProject.contains(name)) {
        println("current project = ${name}")
        apply(plugin = "maven-publish")
        apply(plugin = "java")
        val project = this
        val sourcesJar by tasks.registering(Jar::class) {
            classifier = "sources"
            from(sourceSets.main.get().allSource)
        }

        val configurePublication: Action<MavenPublication> = Action {
            val publication = this
            group = /*Configuration.pluginGroup*/ project.group
            version = Configuration.pluginVersion
            artifactId = project.name
            artifact(sourcesJar.get())
            if ("mavenJava" == publication.name) {
                from(components.getByName("java"))
            }

            pom {
                uri("https://github.com/onion99/transformer")
            }
        }

        afterEvaluate {
            publishing {
                repositories {
                    maven {
                        // change to point to your repo, e.g. http://my.org/repo
                        url = uri("$buildDir/repo")
                    }
                }
                publications {
                    if (plugins.hasPlugin("java-gradle-plugin")){
                        withType(MavenPublication::class).configureEach(configurePublication)
                    }else register("mavenJava", MavenPublication::class).configure(configurePublication)
                }
            }
        }
    }

}