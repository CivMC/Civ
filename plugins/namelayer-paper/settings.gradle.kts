rootProject.name = "namelayer"

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

include("paper")
