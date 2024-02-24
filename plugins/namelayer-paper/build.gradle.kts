plugins {
	id("io.papermc.paperweight.userdev")
}

version = "3.0.6"

dependencies {
	paperweight {
		paperDevBundle("1.18.2-R0.1-SNAPSHOT")
	}

	compileOnly(project(":plugins:civmodcore-paper"))
}
