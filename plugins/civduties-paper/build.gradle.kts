plugins {
    id("io.papermc.paperweight.userdev")
}

version = "1.5.0-SNAPSHOT"

dependencies {
    paperweight {
        paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly(project(":plugins:combattagplus-paper"))
}
