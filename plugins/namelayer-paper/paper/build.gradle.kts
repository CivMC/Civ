plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "NameLayer"
	}
}

dependencies {
	paperDevBundle("1.18.1-R0.1-SNAPSHOT")

	compileOnly("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
}
