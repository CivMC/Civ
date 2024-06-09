plugins {
	id("io.papermc.paperweight.userdev")
}

version = "2.2.2"

dependencies {
	paperweight {
		paperDevBundle(libs.versions.paper)
	}

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:namelayer-paper"))
}
