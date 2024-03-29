val infoModelVersion: String by project
val rsApi: String by project

plugins {
    `java-library`
}


dependencies {
    api(project(":spi"))
    implementation(project(":extensions:ids:ids-spi"))
}


publishing {
    publications {
        create<MavenPublication>("ids-policy-mock") {
            artifactId = "edc.ids-policy-mock"
            from(components["java"])
        }
    }
}