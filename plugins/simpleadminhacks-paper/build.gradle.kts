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
	group = "net.civmc.simpleadminhacks"
	version = "2.0.0-SNAPSHOT"
	description = "SimpleAdminHacks"
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
        civRepo("CivMC/NameLayer")
        civRepo("CivMC/Citadel")
        civRepo("CivMC/CombatTagPlus")
        civRepo("CivMC/BanStick")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://ci.frostcast.net/plugin/repository/everything")
        maven("https://jitpack.io")
	}

	publishing {
		repositories {
			maven {
				name = "GitHubPackages"
				url = uri("https://maven.pkg.github.com/CivMC/SimpleAdminHacks")
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
