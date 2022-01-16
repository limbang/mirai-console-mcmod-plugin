plugins {
    val kotlinVersion = "1.5.30"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.8.3"
}

group = "top.limbang"
version = "1.2.1"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies{
    implementation("org.jsoup:jsoup:1.14.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("ch.qos.logback:logback-classic:1.2.10")
}


