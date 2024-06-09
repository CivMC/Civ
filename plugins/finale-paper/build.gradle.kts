plugins {
	id("io.papermc.paperweight.userdev")
}

repositories {
	maven("https://repo.dmulloy2.net/repository/public")
}

version = "2.1.0"

dependencies {
	paperweight {
		paperDevBundle(libs.versions.paper)
	}

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:combattagplus-paper"))
	compileOnly("com.comphenix.protocol:ProtocolLib:5.2.0-20231209.220838-1")
}
