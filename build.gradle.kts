// KTS教程: https://github.com/gradle/kotlin-dsl-samples/blob/master/samples/maven-publish/build.gradle.kts
import  com.nova.build.Configuration

plugins {
    //alias(libs.plugins.spotless)
    id("maven-publish")
//  id("java")
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
//    classpath("com.nova.sun:main:0.0.2")
    }
}

apply(from = "$rootDir/exclude_other_version.gradle")



allprojects {
    val project = this
    val configurePublication: Action<MavenPublication> = Action {
        val publication = this
        group = Configuration.pluginGroup
        version = Configuration.pluginVersion
        artifactId = project.name
        if ("mavenJava" == publication.name) {
            from(components.findByName("java"))
        }

        pom {
            uri("https://github.com/onion99/transformer")
            /*withXml {
                val xml = this
                exec {
                    commandLine("git","log","--format=%aN %aE")
                }
            }*/
        }
    }
//  println("current project = ${name}")
    val noTransformerProject = arrayOf("app", "Transformers")
    if (!noTransformerProject.contains(name)) {
        /*val sourcesJar by tasks.registering(Jar::class) {
          classifier = "sources"
          from(sourceSets.main.get().allSource)
        }*/
        apply(plugin = "maven-publish")
        apply(plugin = "java")
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
                    /*register("mavenJava", MavenPublication::class) {
                        from(components["java"])
//            artifact(sourcesJar.get())
                        pom {

                        }
                    }*/
                }
            }
        }
    }
//  if(name.equals())
//  apply(plugin = "java")
//  apply(plugin = "kotlin")

}