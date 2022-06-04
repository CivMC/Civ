plugins {
    `java-library`
    id("net.civmc.civgradle.plugin")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    compileOnly("net.civmc.civmodcore:paper:2.0.0-SNAPSHOT:dev-all")
}
