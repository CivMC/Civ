plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "Finale"
	}
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")
    compileOnly("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
    compileOnly("net.civmc.combattagplus:combattagplus-spigot:2.0.0-SNAPSHOT:dev")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0-SNAPSHOT")
}
