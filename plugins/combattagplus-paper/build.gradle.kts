val pluginName: String by project

plugins {
    id("org.sonarqube") version "3.3"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    project.setProperty("archivesBaseName", "$pluginName-$name-$version")

    configure<JavaPluginExtension> {
        withJavadocJar()
        withSourcesJar()

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    repositories {
        mavenCentral()
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://libraries.minecraft.net")

        maven("https://jitpack.io")
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/CivMC/$pluginName")
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
        publications {
            register<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "CivMC_$pluginName")
        property("sonar.organization", "civmc")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
