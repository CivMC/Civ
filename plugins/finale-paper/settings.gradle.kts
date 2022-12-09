pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://papermc.io/repo/repository/maven-public/")
  		maven {
			url = uri("https://maven.pkg.github.com/CivMC/CivGradle")
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}
	}
}

rootProject.name = "finale"

include(":paper")
project(":paper").name = rootProject.name + "-paper"
