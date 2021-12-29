import net.civmc.civgradle.common.util.civRepo

plugins {
	`java-library`
	`maven-publish`
	id("net.civmc.civgradle.plugin") version "1.0.0-SNAPSHOT"
}

group = "net.civmc"
version = "3.0.0-SNAPSHOT"
description = "JukeAlert"

subprojects {
	apply(plugin = "net.civmc.civgradle.plugin")
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")

	group = "net.cimc.namelayer"
	version = "3.0.0-SNAPSHOT"

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(17))
		}
	}

	repositories {
		mavenCentral()

		maven("https://papermc.io/repo/repository/maven-public/")

		civRepo("CivMC/CivModCore")
	}

	publishing {
		repositories {
			maven {
				name = "GitHubPackages"
				url = uri("https://maven.pkg.github.com/CivMC/NameLayer")
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
