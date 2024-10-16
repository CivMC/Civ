plugins {
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

version = "3.2.3"

dependencies {
    paperweight {
        paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
    implementation("org.apache.commons:commons-math3:3.6.1")
}
