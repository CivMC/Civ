plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "RailSwitch"
	}
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    implementation("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
    implementation("net.civmc:namelayer-spigot:3.0.0-SNAPSHOT:dev")
    implementation("net.civmc:citadel:5.0.0-SNAPSHOT:dev")
}
