import net.civmc.civgradle.CivGradleExtension

plugins {
	id("net.civmc.civgradle") version "2.+" apply false
}

subprojects {
	apply(plugin = "net.civmc.civgradle")
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")

	configure<CivGradleExtension> {
		pluginName = project.property("pluginName") as String
	}

	repositories {
		mavenCentral()
		maven("https://repo.civmc.net/repository/maven-public")
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/CivMC/RandomSpawn")
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}

		maven("https://jitpack.io")
	}
}