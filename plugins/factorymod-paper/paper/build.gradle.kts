plugins {
	id("io.papermc.paperweight.userdev")
}

dependencies {
	paperweight {
		paperDevBundle("1.18.2-R0.1-SNAPSHOT")
	}

    compileOnly("net.civmc.civmodcore:civmodcore-paper:2.3.5:dev-all")
	compileOnly("net.civmc.namelayer:namelayer-paper:3.0.3:dev")
	compileOnly("net.civmc.citadel:citadel-paper:5.1.2:dev")
}
