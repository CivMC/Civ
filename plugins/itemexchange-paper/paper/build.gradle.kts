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
	paperDevBundle("1.18.1-R0.1-SNAPSHOT")

	compileOnly("net.civmc.civmodcore:paper:2.0.0-SNAPSHOT:dev-all")

	compileOnly("org.projectlombok:lombok:1.18.22")
	annotationProcessor("org.projectlombok:lombok:1.18.22")
}
