import net.civmc.civgradle.common.util.civRepo

plugins {
    `java-library`
    `maven-publish`
    id("net.civmc.civgradle.plugin") version "1.0.0-SNAPSHOT"
}

// Temporary hack:
// Remove the root build directory
gradle.buildFinished {
	project.buildDir.deleteRecursively()
}

allprojects {
	group = "net.civmc.civmodcore"
	version = "3.0.0-SNAPSHOT"
	description = "RandomSpawn"
}

subprojects {
	apply(plugin = "net.civmc.civgradle.plugin")
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(17))
		}
	}

	repositories {
		mavenCentral()
        civRepo("CivMC/CivModCore")
        civRepo("CivMC/Bastion")
        civRepo("CivMC/WorldBorder")
        civRepo("CivMC/Banstick")
	}

	publishing {
		repositories {
			maven {
				name = "GitHubPackages"
				url = uri("https://maven.pkg.github.com/CivMC/RandomSpawn")
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