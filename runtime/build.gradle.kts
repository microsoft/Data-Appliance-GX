

val infoModelVersion: String by project
val jacksonVersion: String by project
val jerseyVersion: String by project

val securityType: String by rootProject.extra
val iamType: String by rootProject.extra
val configFs: String by rootProject.extra

plugins {
    `java-library`
    id("application")
}

dependencies {
    api(project(":core"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}
