plugins {
    java
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
}

group = "net.azisaba"
version = "0.0.1"

subprojects {
    group = parent!!.version
    version = parent!!.version

    apply {
        plugin("java")
        plugin("java-library")
        plugin("com.github.johnrengelman.shadow")
    }
}

tasks {
    getByName<Test>("test") {
        useJUnitPlatform()
    }
}
