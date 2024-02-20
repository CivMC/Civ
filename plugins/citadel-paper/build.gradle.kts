plugins {
	id("io.papermc.paperweight.userdev")
}

version = "5.2.1"

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:namelayer-paper"))
	compileOnly(project(":plugins:combattagplus-paper"))
	compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.9")
}
