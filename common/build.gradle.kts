repositories {
    mavenCentral()
}

dependencies {
    // gson
    api("com.google.code.gson:gson:2.9.0")
    compileOnlyApi("org.jetbrains:annotations:23.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
