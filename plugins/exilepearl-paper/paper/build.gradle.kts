plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "ExilePearl"
	}
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    compileOnly("net.civmc.civmodcore:paper:2.0.0-SNAPSHOT:dev-all")
    compileOnly("net.civmc.namelayer:paper:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc.citadel:paper:5.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc.civchat2:paper:2.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc.bastion:paper:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc.combattagplus:paper:2.0.0-SNAPSHOT:dev")
	compileOnly("net.civmc.banstick:paper:2.0.0-SNAPSHOT:dev")
	compileOnly("net.civmc:randomspawn:3.0.0-SNAPSHOT:dev")
	compileOnly("net.civmc:worldborder:2.0.0-SNAPSHOT:dev")

	compileOnly("com.github.DieReicheErethons:Brewery:3.1")
}
