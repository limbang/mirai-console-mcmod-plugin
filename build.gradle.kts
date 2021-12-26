plugins {
    val kotlinVersion = "1.5.30"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.8.3"
}

group = "top.limbang"
version = "1.1.5"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies{
    implementation("org.jsoup:jsoup:1.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
}


