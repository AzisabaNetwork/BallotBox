plugins {
    java
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

group = "net.azisaba"
version = "1.0.1"

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
    test {
        useJUnitPlatform()
    }
}
