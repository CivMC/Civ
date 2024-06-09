plugins {
	id("io.papermc.paperweight.userdev")
}

version = "5.2.4"

dependencies {
	paperweight {
		paperDevBundle(libs.versions.paper)
	}

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:namelayer-paper"))
	compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.9")
}
