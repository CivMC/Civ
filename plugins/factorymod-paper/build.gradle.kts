plugins {
	id("io.papermc.paperweight.userdev")
}

version = "3.1.0"

dependencies {
	paperweight {
		paperDevBundle(libs.versions.paper)
	}

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:namelayer-paper"))
	compileOnly(project(":plugins:citadel-paper"))
}
