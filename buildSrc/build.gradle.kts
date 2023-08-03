plugins {
    `kotlin-dsl`
}

repositories {
    maven(uri("https://repository.inkept.cn/repository/maven-releases"))
    maven(uri("https://repository.inkept.cn/repository/inke-gz-release"))
    maven(uri("https://repository.inkept.cn/repository/inke-gz-inf"))
    maven(uri("https://repo1.maven.org/maven2/"))
    maven(uri("https://maven.aliyun.com/repository/public"))
    maven(uri("https://maven.aliyun.com/repository/google"))
    mavenCentral()
    jcenter()
    maven(uri("https://oss.sonatype.org/content/repositories/snapshots"))
}