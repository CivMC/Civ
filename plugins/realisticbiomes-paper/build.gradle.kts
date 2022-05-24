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
	group = "net.civmc.realisticbiomes"
	version = "3.0.0-SNAPSHOT"
	description = "RealisticBiomes"
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

        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.maven.apache.org/maven2/")
	}

	publishing {
		repositories {
			maven {
				name = "GitHubPackages"
				url = uri("https://maven.pkg.github.com/CivMC/RealisticBiomes")
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
