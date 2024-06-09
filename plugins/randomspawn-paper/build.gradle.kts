plugins {
	id("io.papermc.paperweight.userdev")
}

version = "3.0.4"

dependencies {
	paperweight {
		paperDevBundle(libs.versions.paper)
	}

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:bastion-paper"))
	compileOnly(project(":plugins:banstick-paper"))
}
