plugins {
	id("io.papermc.paperweight.userdev")
	id("com.github.johnrengelman.shadow")
}

version = "2.0.1"

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:namelayer-paper"))

	implementation("com.github.seancfoley:ipaddress:2.0.2")
	implementation("org.jsoup:jsoup:1.13.1")
}
