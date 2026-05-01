plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.gradleup.shadow") version "9.0.0"
    application
}

group = "com.toolbox"
version = "0.2.0"

val mainClassPath = "com.toolbox.ApplicationKt"

application {
    mainClass.set(mainClassPath)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = mainClassPath
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("server.jar")
    manifest {
        attributes["Main-Class"] = mainClassPath
    }
}

tasks.named("build") {
    dependsOn(tasks.shadowJar)
}

dependencies {
    implementation("io.ktor:ktor-server-core:3.1.0")
    implementation("io.ktor:ktor-server-netty:3.1.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.0")
    implementation("io.ktor:ktor-server-websockets:3.1.0")
    implementation("io.ktor:ktor-server-cors:3.1.0")

    implementation("org.jetbrains.exposed:exposed-core:0.57.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.57.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.57.0")

    implementation("com.h2database:h2:2.3.232")

    implementation("ch.qos.logback:logback-classic:1.5.12")

    implementation("com.auth0:java-jwt:4.4.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
}
