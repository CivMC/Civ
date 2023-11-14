import net.civmc.civgradle.CivGradleExtension

plugins {
	id("net.civmc.civgradle") version "2.+" apply false
    id("com.gradle.enterprise") version("3.15")
}

subprojects {
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")
	apply(plugin = "net.civmc.civgradle")

	configure<CivGradleExtension> {
		pluginName = project.property("pluginName") as String
	}

	repositories {
		mavenCentral()
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://libraries.minecraft.net")

		maven("https://jitpack.io")
	}
}

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}
