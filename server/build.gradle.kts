val log4jVersion = "2.19.0"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":common"))

    // netty
    api("io.netty:netty-all:4.1.92.Final")

    // logging
    api("org.apache.logging.log4j:log4j-api:$log4jVersion")
    api("org.apache.logging.log4j:log4j-core:$log4jVersion")
    api("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    api("org.slf4j:slf4j-api:2.0.6")

    // config
    api("org.yaml:snakeyaml:1.33")

    // mariadb-client
    api("org.mariadb.jdbc:mariadb-java-client:3.1.1")
}

tasks {
    withType<ProcessResources> {
        from(sourceSets.main.get().resources.srcDirs) {
            include("**")
            val tokenReplacementMap = mapOf(
                    "VERSION" to project.version,
                    "NAME" to project.parent!!.name,
            )
            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }
        filteringCharset = "UTF-8"
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(project.parent!!.projectDir) { include("LICENSE") }
    }

    shadowJar {
        manifest {
            attributes("Main-Class" to "net.azisaba.ballotbox.server.Main")
        }

        archiveFileName.set("BallotBox-server-${project.version}.jar")
    }
}
