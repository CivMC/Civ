plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "HiddenOre"
	}
}

dependencies {
	paperDevBundle("1.18.1-R0.1-SNAPSHOT")
}
