plugins {
	id("io.papermc.paperweight.userdev")
	id("com.github.johnrengelman.shadow")
	id("xyz.jpenilla.run-paper")
}

version = "2.0.1"

dependencies {
	implementation(project(":plugins:civspy-api"))

	paperweight {
		paperDevBundle(libs.versions.paper)
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
