plugins {
    java
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
}

group = "net.azisaba"
version = "0.0.1"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

subprojects {
    group = parent!!.version
    version = parent!!.version

    apply {
        plugin("java")
        plugin("java-library")
        plugin("com.github.johnrengelman.shadow")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks {
    getByName<Test>("test") {
        useJUnitPlatform()
    }
}
