plugins {
	id("net.civmc.civgradle")
	id("io.papermc.paperweight.userdev")
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	compileOnly("net.civmc.civmodcore:paper:2.0.0-SNAPSHOT:dev-all")
	compileOnly("net.civmc.namelayer:paper:3.0.0-SNAPSHOT:dev")
	compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.9")
}
