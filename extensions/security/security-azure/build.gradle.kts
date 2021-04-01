plugins {
    `java-library`
}

dependencies {
    api(project(":spi"))
    implementation("com.azure:azure-security-keyvault-secrets:4.2.3")
    implementation("com.azure:azure-identity:1.2.0")
}


