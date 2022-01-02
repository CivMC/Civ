plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "SimpleAdminHacks"
	}
}

dependencies {
	paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    compileOnly("net.civmc.civmodcore:paper:2.0.0-SNAPSHOT:dev-all")
    compileOnly("net.civmc.namelayer:paper:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc.citadel:paper:5.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc.combattagplus:paper:2.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc.banstick:paper:2.0.0-SNAPSHOT:dev")

	compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0-SNAPSHOT")

}
