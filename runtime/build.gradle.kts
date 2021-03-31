plugins {
    `java-library`
    id("application")
}

dependencies {
    api(project(":core"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}
