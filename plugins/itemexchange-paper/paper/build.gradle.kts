plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "ItemExchange"
	}
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	compileOnly("net.civmc.civmodcore:paper:2.0.0-SNAPSHOT:dev-all")
	compileOnly("net.civmc:namelayer-spigot:3.0.0-SNAPSHOT:dev")
	compileOnly("net.civmc:citadel:5.0.0-SNAPSHOT:dev")
	compileOnly("net.cimc.jukealert:paper:3.0.0-SNAPSHOT:dev")

	compileOnly("org.projectlombok:lombok:1.18.24")
	annotationProcessor("org.projectlombok:lombok:1.18.24")
}
