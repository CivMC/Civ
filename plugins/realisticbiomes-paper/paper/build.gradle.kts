plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "RealisticBiomes"
	}
}

dependencies {
	paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    implementation("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
    implementation("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
    implementation("org.apache.commons:commons-math3:3.6.1")
}
