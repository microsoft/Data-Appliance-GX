import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.container.*

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

    implementation(project(":extensions:security:security-fs"))
    implementation(project(":extensions:iam:oauth2"))
    implementation(project(":extensions:configuration:configuration-fs"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")

}

// building docker images for the Demo (which uses the file system)
val createDockerfile by tasks.creating(Dockerfile::class) {
    from("openjdk:11-jre-slim")
    runCommand("mkdir /app")
    copyFile("./build/libs/dagx-demo.jar", "/app/dagx-runtime.jar")
    runCommand("mkdir -p /etc/dagx/secrets")
    copyFile("secrets/dagx-vault.properties", "/app/dagx-vault.properties")
    copyFile("secrets/dagx-test-keystore.jks", "/app/dagx-test-keystore.jks")

    environmentVariable("DAGX_VAULT", "/app/dagx-vault.properties")
    environmentVariable("DAGX_KEYSTORE", "/app/dagx-test-keystore.jks")
    environmentVariable("DAGX_KEYSTORE_PASSWORD", "test123")

    entryPoint("java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/dagx-runtime.jar")
}

val buildImage by tasks.creating(DockerBuildImage::class) {
    dependsOn("shadowJar", createDockerfile)
    dockerFile.set(project.file("${buildDir}/docker/Dockerfile"))
    inputDir.set(project.file("."))
    images.add("microsoft/dagx")
}

val createDemoContainer by tasks.creating(DockerCreateContainer::class) {
    dependsOn(buildImage)
    targetImageId(buildImage.imageId)
    hostConfig.portBindings.set(listOf("8181:8181"))
    hostConfig.autoRemove.set(true)
    containerName.set("dagx-demo")
}

// start runtime demo in docker
val startDemo by tasks.creating(DockerStartContainer::class) {
    doFirst {
        System.setProperty("security.type", "fs")

    }
    dependsOn(createDemoContainer)
    targetContainerId(createDemoContainer.containerId)
}

application {
    @Suppress("DEPRECATION")
    mainClassName= "com.microsoft.dagx.runtime.DagxRuntime"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("dagx-demo.jar")
}
