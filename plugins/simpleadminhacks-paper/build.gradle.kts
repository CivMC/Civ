plugins {
	id("io.papermc.paperweight.userdev")
}

version = "2.3.2"

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:namelayer-paper"))
	compileOnly(project(":plugins:citadel-paper"))
	compileOnly(project(":plugins:combattagplus-paper"))
	compileOnly(project(":plugins:banstick-paper"))
	compileOnly(project(":plugins:bastion-paper"))
	compileOnly(project(":plugins:exilepearl-paper"))

	compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
}
