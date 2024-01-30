rootProject.name = "civspy"

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://papermc.io/repo/repository/maven-public/")
	}
}

include(":api")
include(":platform:bungee")
include(":platform:paper")
