plugins {
	id("net.civmc.civgradle")
	id("io.papermc.paperweight.userdev")
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	compileOnly("net.civmc.civmodcore:civmodcore-paper:2.3.5:dev-all")
	compileOnly("net.civmc.namelayer:namelayer-paper:3.0.4:dev")
	compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.9")
}
