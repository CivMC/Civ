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
    compileOnly(project(":plugins:bastion-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:citadel-paper"))
    compileOnly(project(":plugins:combattagplus-paper"))

    compileOnly(libs.protocollib)
}
