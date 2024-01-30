rootProject.name = "civspy"

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://papermc.io/repo/repository/maven-public/")
	}
}

plugins {
	id("com.gradle.enterprise") version("3.16")
}

rootProject.name="civspy"

include(":api")
include(":platform:bungee")
include(":platform:paper")
project(":platform:paper").name = rootProject.name + "-paper"
