plugins {
	id("io.papermc.paperweight.userdev")
}

version = "2.1.0"

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:combattagplus-paper"))
	compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
}
