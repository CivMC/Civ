plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "RandomSpawn"
	}
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")
    compileOnly("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
    compileOnly("net.civmc:worldborder:2.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:bastion:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:banstick:2.0.0-SNAPSHOT:dev")
}