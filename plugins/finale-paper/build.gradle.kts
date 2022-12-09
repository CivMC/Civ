import net.civmc.civgradle.CivGradleExtension

plugins {
	id("net.civmc.civgradle") version "2.+" apply false
}

// Temporary hack:
// Remove the root build directory
gradle.buildFinished {
	project.buildDir.deleteRecursively()
}

subprojects {
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")
	apply(plugin = "net.civmc.civgradle")

	configure<CivGradleExtension> {
		pluginName = project.property("pluginName") as String
	}

	repositories {
		mavenLocal()
		mavenCentral()
		//maven("https://repo.civmc.net/repository/maven-public")
		maven("https://repo.dmulloy2.net/content/groups/public/")

		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/CivMC/CivModCore")
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}

		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/CivMC/Finale")
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}
	}
}
