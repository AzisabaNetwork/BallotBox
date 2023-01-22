repositories {
    mavenCentral()
}

dependencies {
    // gson
    api("com.google.code.gson:gson:2.9.1")
    compileOnlyApi("org.jetbrains:annotations:24.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
