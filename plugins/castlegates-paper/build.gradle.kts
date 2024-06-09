plugins {
	id("io.papermc.paperweight.userdev")
}

version = "2.0.2"

dependencies {
	paperweight {
		paperDevBundle(libs.versions.paper)
	}

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:namelayer-paper"))
	compileOnly(project(":plugins:citadel-paper"))
	compileOnly(project(":plugins:bastion-paper"))
	compileOnly(project(":plugins:jukealert-paper"))
}
