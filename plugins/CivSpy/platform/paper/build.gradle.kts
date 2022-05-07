val pluginName: String by project

plugins {
	id("com.github.johnrengelman.shadow")
	id("xyz.jpenilla.run-paper") version "1.0.6"
}

repositories {
	maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
	implementation(project(":api"))

	compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
}

tasks {
	shadowJar {
		exclude("org.slf4j")

		relocate("org.postgresql", "com.programmerdan.minecraft.civspy.repack.postgresql")
		relocate("org.checkerframework", "com.programmerdan.minecraft.civspy.repack.checkerframework")
		relocate("com.zaxxer.hikari", "com.programmerdan.minecraft.civspy.repack.hikari")
	}

	processResources {
		filesMatching("plugin.yml") {
			expand(mapOf(
				"name" to pluginName,
				"version" to version,
			))
		}
	}

	runServer {
		minecraftVersion("1.18")
	}
}
