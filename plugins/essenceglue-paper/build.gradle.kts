plugins {
	id("io.papermc.paperweight.userdev")
}

version = "2.0.0-SNAPSHOT"

dependencies {
	paperweight {
		paperDevBundle(libs.versions.paper)
	}

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly("com.github.NuVotifier.NuVotifier:nuvotifier-bukkit:2.7.2")
	compileOnly("com.github.NuVotifier.NuVotifier:nuvotifier-api:2.7.2")
	compileOnly(project(":plugins:banstick-paper"))
	compileOnly(project(":plugins:exilepearl-paper"))
}
