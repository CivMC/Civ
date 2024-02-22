plugins {
	id("io.papermc.paperweight.userdev")
}

version = "3.1.0"

dependencies {
	paperweight {
		paperDevBundle("1.18.2-R0.1-SNAPSHOT")
	}

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:namelayer-paper"))
	compileOnly(project(":plugins:citadel-paper"))
}
