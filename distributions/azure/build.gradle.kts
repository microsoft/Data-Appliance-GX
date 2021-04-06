import com.bmuschko.gradle.docker.tasks.image.*
import com.bmuschko.gradle.docker.tasks.container.*

import java.io.FileInputStream
import java.util.*

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.bmuschko.docker-remote-api") version "6.7.0"
}


dependencies {
    implementation(project(":runtime"))
    implementation(project(":extensions:protocol:web"))
    implementation(project(":extensions:control-http"))
    implementation(project(":extensions:iam:oauth2"))

    implementation(project(":extensions:metadata:metadata-memory"))
    implementation(project(":extensions:transfer:transfer-core"))
    implementation(project(":extensions:transfer:transfer-nifi"))
    implementation(project(":extensions:ids"))
    implementation(project(":extensions:demo:demo-nifi"))

    implementation(project(":extensions:security:security-azure"))

    implementation(project(":extensions:iam:oauth2"))
    implementation(project(":extensions:configuration:configuration-fs"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")

}

val githubPropertiesFile = "github.properties"
val azurePropertiesFile = "azure.properties"
var email = ""
var user = "microsoft"
var pwd = ""
var url = ""
var imageName = ""


// generate docker file
val createDockerfile by tasks.creating(Dockerfile::class) {

    readGithubConfig(this, this@Build_gradle)

    //read config for azure keyvault
    val prop = Properties()

    val configFile = project.file(azurePropertiesFile)
    if (!configFile.exists()) {
//            throw IllegalArgumentException("No $azurePropertiesFile file was found! Aborting...")
        println("WARNING: No $azurePropertiesFile file was found, Azure Authentication will need to be passed via environment variables!")
    } else {
        val fis = FileInputStream(configFile)
        prop.load(fis)

    }

    var clientId = prop.getProperty("dagx.vault.clientid", "")
    val tenantId = prop.getProperty("dagx.vault.tenantid", "")
    val certFile = prop.getProperty("dagx.vault.certificate", "")
    val vaultName = prop.getProperty("dagx.vault.name", "")

    if (certFile != "" && !project.file(certFile).exists()) {
//        throw kotlin.IllegalArgumentException("File $certFile does not exist!")
        println("WARNING: certificate file not found! Certificate needs to be copied manually to /app/azure-vault-cert.pfx!")
    }


    from("openjdk:11-jre-slim")
    runCommand("mkdir /app")
    copyFile("./build/libs/dagx-azure.jar", "/app/dagx-azure.jar")

    copyFile(certFile, "/app/azure-vault-cert.pfx")

    environmentVariable("DAGX_VAULT_CLIENTID", clientId)
    environmentVariable("DAGX_VAULT_TENANTID", tenantId)
    environmentVariable("DAGX_VAULT_CERTIFICATE", "/app/azure-vault-cert.pfx")
    environmentVariable("DAGX_VAULT_NAME", vaultName)
    exposePort(8181)
    entryPoint("java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/dagx-azure.jar")
}

// build the image
val buildAzure by tasks.creating(DockerBuildImage::class) {
    dependsOn("shadowJar", createDockerfile)
    dockerFile.set(project.file("${buildDir}/docker/Dockerfile"))
    inputDir.set(project.file("."))
    images.add(imageName)
}

// create azure container
val createAzureContainer by tasks.creating(DockerCreateContainer::class) {
    dependsOn(buildAzure)
    targetImageId(buildAzure.imageId)
    hostConfig.portBindings.set(listOf("8181:8181"))
    hostConfig.autoRemove.set(true)
    containerName.set("dagx-azure")
}

// start runtime azure in docker
val startAzure by tasks.creating(DockerStartContainer::class) {
    dependsOn(createAzureContainer)
    targetContainerId(createAzureContainer.containerId)
}

//publish to github
val publishAzure by tasks.creating(DockerPushImage::class) {
    dependsOn(buildAzure)

    registryCredentials.email.set(email)
    registryCredentials.username.set(user)
    registryCredentials.password.set(pwd)
    registryCredentials.url.set(imageName)
    images.add(imageName)
}

application {
    @Suppress("DEPRECATION")
    mainClassName = "com.microsoft.dagx.runtime.DagxRuntime"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("dagx-azure.jar")
}

fun readGithubConfig(dockerfile: Dockerfile, buildGradle: Build_gradle) {
    val githubConfigFile = dockerfile.project.file(buildGradle.githubPropertiesFile)
    if (!githubConfigFile.exists()) {
        println("WARNING: No ${buildGradle.githubPropertiesFile} file was found, default will be used. Publishing won't be available!")
    } else {
        val fis = FileInputStream(githubConfigFile)
        val prop = Properties()
        prop.load(fis)
        buildGradle.email = prop.getProperty("email")
        buildGradle.user = prop.getProperty("user")
        buildGradle.pwd = prop.getProperty("password")
        buildGradle.url = prop.getProperty("url")
    }
    buildGradle.imageName = "${buildGradle.user}/dagx-azure:latest"

    if (buildGradle.url != "") {
        buildGradle.imageName = "${buildGradle.url}/${buildGradle.imageName}"
    }

    println("Will use the following docker config:")
    println("  - URL: ${buildGradle.url}")
    println("  - User: ${buildGradle.user}")
    println("  - Email: ${buildGradle.email}")
    println("  - Image: ${buildGradle.imageName}")
}
