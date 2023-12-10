pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://repo.civmc.net/repository/maven-public/")
		maven("https://papermc.io/repo/repository/maven-public/")
	}
}

plugins {
	id("com.gradle.enterprise") version("3.15")
}

rootProject.name = "civmodcore"

include(":paper")
project(":paper").name = rootProject.name + "-paper"
