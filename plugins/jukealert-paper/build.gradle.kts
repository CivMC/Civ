import net.civmc.civgradle.common.util.civRepo

plugins {
    `java-library`
    `maven-publish`
	id("net.civmc.civgradle.plugin") version "1.0.0-SNAPSHOT"
}

group = "net.civmc"
version = "3.0.3"
description = "JukeAlert"

subprojects {
	apply(plugin = "net.civmc.civgradle.plugin")
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")

	group = "net.civmc.jukealert"
	version = "3.0.3"

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(17))
		}
	}

	repositories {
		mavenCentral()

		maven("https://papermc.io/repo/repository/maven-public/")

		civRepo("CivMC/CivModCore")
		civRepo("CivMC/NameLayer")
		civRepo("CivMC/Citadel")
	}

	publishing {
		repositories {
			maven {
				name = "GitHubPackages"
				url = uri("https://maven.pkg.github.com/CivMC/JukeAlert")
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
}
