// KTS教程: https://github.com/gradle/kotlin-dsl-samples/blob/master/samples/maven-publish/build.gradle.kts
import  com.nova.build.Configuration
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

// ------------------------------------------------------------------------
// 为下面配置AllProjects做编译导入支持
// ------------------------------------------------------------------------
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
        classpath("com.github.Onion99:Transformers:1.2")
    }
}

apply(from = "$rootDir/exclude_other_version.gradle")

fun Project.getLatestGitTag(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            standardOutput = stdout
        }
        stdout.toString().trim().removePrefix("v")
    } catch (e: Exception) {
        // 如果没有 tag，返回默认版本
        Configuration.pluginVersion
    }
}

fun Project.getGitVersion(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            // 获取最近的 tag 和提交信息
            commandLine("git", "describe", "--tags", "--long")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Exception) {
        Configuration.pluginVersion
    }
}

allprojects {
    val noTransformerProject = arrayOf("app", "Transformers","transformer","plugin")
    configurations{
        val attrGroup = Attribute.of("pluginGroup", String::class.java)
        val attrVersion = Attribute.of("pluginVersion", String::class.java)
        create("pluginAttr"){
            attributes {
                attribute(attrGroup,Configuration.pluginGroup)
                attribute(attrVersion,getLatestGitTag())
            }
        }
    }
    if (!noTransformerProject.contains(name)) {
        println("current project = ${name}")
        apply(plugin = "maven-publish")
        apply(plugin = "java")
        val project = this
        
        // 配置 Javadoc 任务
        tasks.withType<Javadoc> {
            options {
                this as StandardJavadocDocletOptions
                addStringOption("Xdoclint:none", "-quiet")
                addStringOption("encoding", "UTF-8")
                addStringOption("charSet", "UTF-8")
                addBooleanOption("html5", true)
                // 忽略 @hide 标签等错误
                (this as CoreJavadocOptions).addStringOption("tag", "hide:X")
                (this as CoreJavadocOptions).addStringOption("tag", "param:X")
                (this as CoreJavadocOptions).addStringOption("tag", "return:X")
            }
            // 忽略 Javadoc 错误
            isFailOnError = false
        }
        
        // 添加 sources jar
        val sourcesJar by tasks.registering(Jar::class) {
            archiveClassifier.set("sources")
            from(sourceSets.main.get().allSource)
        }

        // 添加 javadoc jar
        val javadocJar by tasks.registering(Jar::class) {
            archiveClassifier.set("javadoc")
            from(tasks.named("javadoc"))
        }

        val configurePublication: Action<MavenPublication> = Action {
            val publication = this
            // 对应发布的包域
            group = Configuration.pluginGroup
            // 使用最近的 Git tag 作为版本号
            version = project.getLatestGitTag()
            artifactId = project.name
            
            // 根据不同的 publication 类型选择合适的配置
            if (plugins.hasPlugin("java-gradle-plugin")) {
                // 对于 gradle plugin，不需要手动添加 java component
                artifact(sourcesJar.get())
                artifact(javadocJar.get())
            } else {
                // 对于普通的 java 库
                from(components.getByName("java"))
                artifact(sourcesJar.get())
                artifact(javadocJar.get())
            }

            // 配置 POM
            pom {
                name.set(project.name)
                description.set("Transformer plugin for Android")
                url.set("https://github.com/onion99/transformer")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("onion99")
                        name.set("onion99")
                        email.set("891564341@qq.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/onion99/transformer.git")
                    developerConnection.set("scm:git:ssh://github.com/onion99/transformer.git")
                    url.set("https://github.com/onion99/transformer")
                }
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