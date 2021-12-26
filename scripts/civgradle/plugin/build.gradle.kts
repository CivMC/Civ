plugins {
    kotlin("jvm") version "1.6.0"
    id("java-gradle-plugin")
    `maven-publish`
}

group = "net.civmc.civgradle"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", "1.6.0"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

gradlePlugin {
    plugins {
        create("net.civmc.civgradle") {
            id = "net.civmc.civgradle.plugin"
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
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}