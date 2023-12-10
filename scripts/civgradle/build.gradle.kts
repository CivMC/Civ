plugins {
    kotlin("jvm") version "1.9.21"
    id("java-gradle-plugin")
    `maven-publish`
}

group = "net.civmc"
version = "3.0.1"

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib", "1.6.0"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2") {
        constraints {
            implementation("commons-io:commons-io:2.12.0") {
                because("Resolves CVE-2021-29425") // https://devhub.checkmarx.com/cve-details/CVE-2021-29425/
            }
        }
    }

    // Keep these plugin versions on the classpath, so we can update them all at once if needed.
    implementation("com.gradle.enterprise:com.gradle.enterprise.gradle.plugin:3.15.1")
    implementation("io.papermc.paperweight.userdev:io.papermc.paperweight.userdev.gradle.plugin:1.5.5") // https://github.com/PaperMC/paperweight/tags
    implementation("xyz.jpenilla.run-paper:xyz.jpenilla.run-paper.gradle.plugin:2.1.0") // https://github.com/jpenilla/run-task/releases
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
}

gradlePlugin {
    plugins {
        create("net.civmc.civgradle") {
            id = "net.civmc.civgradle"
            implementationClass = "net.civmc.civgradle.CivGradlePlugin"
            version = version
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/CivMC/CivGradle")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        val targetRepo = if (version.toString().endsWith("SNAPSHOT")) "maven-snapshots" else "maven-releases"
        maven {
            name = "CivMC"
            url = uri("https://repo.civmc.net/repository/$targetRepo/")
            credentials {
                username = System.getenv("CIVMC_NEXUS_USER")
                password = System.getenv("CIVMC_NEXUS_PASSWORD")
            }
        }
    }
}
