plugins {
	id("io.papermc.paperweight.userdev")
	id("xyz.jpenilla.run-paper")
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

    compileOnly("net.civmc.civmodcore:civmodcore-paper:2.3.0:dev-all")
	compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
    implementation("org.apache.commons:commons-math3:3.6.1")
}
