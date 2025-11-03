
plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"

    id("io.ktor.plugin") version "3.3.1"

    id("io.kotest") version "6.0.4"

    id("io.gitlab.arturbosch.detekt") version "1.23.8"

    application
}

repositories {
    mavenCentral()
}

dependencyLocking {
    // Cannot enable LockMode.STRICT if we want IntelliJ to be able to download sources.
    lockAllConfigurations()
}

dependencies {
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-auth")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-encoding")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    testImplementation("io.kotest:kotest-framework-engine:6.0.4")
    testImplementation("io.kotest:kotest-runner-junit5:6.0.4")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "org.example.AppKt"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

detekt {
    toolVersion = "1.23.8"
    buildUponDefaultConfig = true
    autoCorrect = true

    config.from(files(rootProject.file("detekt-config.yaml")))

    source.setFrom(
        project.sourceSets["main"].kotlin,
        project.sourceSets["test"].kotlin,
    )
}

// Use the type-aware tasks, not the default detekt task.
tasks.named("check") {
    dependsOn(
        tasks.named("detektMain"),
        tasks.named("detektTest")
    )
}

tasks.named("detekt") {
    enabled = false
}