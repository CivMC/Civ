plugins {
	id("net.civmc.civgradle")
	id("io.papermc.paperweight.userdev")
}

dependencies {
    paperDevBundle("1.18.2-R0.1-SNAPSHOT")
    compileOnly("net.civmc.civmodcore:paper:2.0.0-SNAPSHOT:dev-all")
    compileOnly("net.civmc.combattagplus:paper:2.0.0-SNAPSHOT:dev")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
}
