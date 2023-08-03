// KTS教程: https://github.com/gradle/kotlin-dsl-samples/blob/master/samples/maven-publish/build.gradle.kts
plugins {
    //alias(libs.plugins.spotless)
  id("maven-publish")
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
//apply(from = "$rootDir/exclude_other_version.gradle")
//allprojects {
////  println("current project = ${name}")
//  val noTransformerProject = arrayOf("app","Transformers")
//  if(!noTransformerProject.contains(name)){
//    group = "com.nova.sun"
//    version = "0.0.2"
//    afterEvaluate {
//      publishing{
//        repositories {
//          maven {
//            // change to point to your repo, e.g. http://my.org/repo
//            url = uri("$buildDir/repo")
//          }
//        }
//        publications {
//          register("mavenJava", MavenPublication::class) {
//            from(components["java"])
//            artifact(sourcesJar.get())
//            pom {
//
//            }
//          }
//        }
//      }
//    }
//  }
////  if(name.equals())
////  apply(plugin = "java")
////  apply(plugin = "kotlin")
//
//}