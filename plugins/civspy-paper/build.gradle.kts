plugins {
	id("io.papermc.paperweight.userdev")
	id("com.github.johnrengelman.shadow")
	id("xyz.jpenilla.run-paper")
}

group = "net.civmc.civspy"
version = "2.0.1"

repositories {
	maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
	implementation(project(":plugins:civspy-api"))

	paperweight {
		paperDevBundle("1.18.2-R0.1-SNAPSHOT")
	}
}

tasks {
	shadowJar {
		dependencies {
			exclude(dependency("org.slf4j::"))
		}

		relocate("org.postgresql", "com.programmerdan.minecraft.civspy.repack.postgresql")
		relocate("org.checkerframework", "com.programmerdan.minecraft.civspy.repack.checkerframework")
		relocate("com.zaxxer.hikari", "com.programmerdan.minecraft.civspy.repack.hikari")
	}
}
