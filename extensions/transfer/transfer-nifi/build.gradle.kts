/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

val awsVersion: String by project

plugins {
    `java-library`
}


dependencies {
    api(project(":spi"))
    implementation(project(":extensions:schema"))

//    testImplementation(project(":extensions:catalog:catalog-atlas"))
    testImplementation("com.azure:azure-storage-blob:12.6.0")
//    testImplementation(project(":extensions:catalog:catalog-atlas-dataseed"))
    implementation(platform("software.amazon.awssdk:bom:${awsVersion}"))
    implementation("software.amazon.awssdk:s3")
}


