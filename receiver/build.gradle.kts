repositories {
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/groups/public/")
    }
}

dependencies {
    // gson
    api(project(":common"))
    api("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
}

tasks {
    withType<ProcessResources> {
        from(sourceSets.main.get().resources.srcDirs) {
            include("**")
            val tokenReplacementMap = mapOf(
                    "VERSION" to project.version
            )
            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }
        filteringCharset = "UTF-8"
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    shadowJar {
        archiveFileName.set("BallotBoxReceiver.jar")
    }
}
