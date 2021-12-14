rootProject.name = "namelayer"

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://papermc.io/repo/repository/maven-public/")
	}
}

include("namelayer-spigot")
