plugins {
	id("java-library")
	id("io.papermc.paperweight.userdev")
	id("com.github.johnrengelman.shadow")
}

version = "5.2.0-SNAPSHOT"

dependencies {
	paperweight {
		paperDevBundle("1.20.4-R0.1-SNAPSHOT")
	}

	api("com.comphenix.protocol:ProtocolLib:${project.version}")
}
