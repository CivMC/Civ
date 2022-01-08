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
    compileOnly("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
    compileOnly("net.civmc:namelayer-spigot:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:citadel:5.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:civchat2:2.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:bastion:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:randomspawn:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:worldborder:2.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc.combattagplus:combattagplus-spigot:2.0.0-SNAPSHOT:dev")
    compileOnly("com.github.DieReicheErethons:Brewery:3.1")
    compileOnly("net.civmc:banstick:2.0.0-SNAPSHOT:dev")
}
