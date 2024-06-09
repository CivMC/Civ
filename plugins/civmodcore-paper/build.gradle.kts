plugins {
	id("io.papermc.paperweight.userdev")
	id("com.github.johnrengelman.shadow")
	id("xyz.jpenilla.run-paper")
}

version = "3.0.6"

dependencies {
	paperweight {
		paperDevBundle(libs.versions.paper)
	}

	api("co.aikar:acf-bukkit:0.5.1-SNAPSHOT")
	api("com.mojang:datafixerupper:1.0.20")
	api("com.zaxxer:HikariCP:5.0.1")
	api("co.aikar:taskchain-bukkit:3.7.2")
	api("org.apache.commons:commons-lang3:3.12.0")
	api("org.apache.commons:commons-collections4:4.4")
	api("com.google.code.findbugs:jsr305:3.0.2")

	compileOnly("it.unimi.dsi:fastutil:8.5.8")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}
