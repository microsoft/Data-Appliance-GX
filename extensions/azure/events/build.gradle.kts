/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

plugins {
    `java-library`
}

val eventGridSdkVersion: String by project

dependencies {
    api(project(":core:spi"))
    implementation(project(":core:schema"))
    implementation(project(":common:util"))
    implementation("com.azure:azure-messaging-eventgrid:${eventGridSdkVersion}")
}


publishing {
    publications {
        create<MavenPublication>("azure.events") {
            artifactId = "edc.events.azure"
            from(components["java"])
        }
    }
}