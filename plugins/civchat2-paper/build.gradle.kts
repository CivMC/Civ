plugins {
	id("io.papermc.paperweight.userdev")
}

version = "2.2.0"

dependencies {
	paperweight {
		paperDevBundle("1.18.2-R0.1-SNAPSHOT")
	}

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:namelayer-paper"))
}
