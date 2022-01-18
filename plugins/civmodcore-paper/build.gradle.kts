plugins {
	id("org.sonarqube") version "3.3" apply false
}

allprojects {
	group = "net.civmc.civmodcore"
	version = "2.0.0-SNAPSHOT"
	description = "CivModCore"
}

subprojects {
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")
	apply(plugin = "org.sonarqube")

	project.setProperty("archivesBaseName", "CivModCore-$name")

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
				url = uri("https://maven.pkg.github.com/CivMC/CivModCore")
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

	configure<org.sonarqube.gradle.SonarQubeExtension> {
		properties {
			property("sonar.projectKey", "CivMC_CivModCore")
			property("sonar.organization", "civmc")
			property("sonar.host.url", "https://sonarcloud.io")
		}
	}
}
