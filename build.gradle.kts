plugins {
    val kotlinVersion = "1.9.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.16.0"
}

group = "top.limbang.mcmod"
version = "2.0.10"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies{
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.4")
}


