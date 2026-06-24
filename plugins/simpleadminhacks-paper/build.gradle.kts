plugins {
    alias(libs.plugins.paper.userdev)
}

version = "2.3.2"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:citadel-paper"))
    compileOnly(project(":plugins:combattagplus-paper"))
    compileOnly(project(":plugins:banstick-paper"))
    compileOnly(project(":plugins:bastion-paper"))
    compileOnly(project(":plugins:exilepearl-paper"))
    compileOnly(project(":plugins:factorymod-paper"))
    compileOnly(libs.placeholderapi)
    compileOnly(libs.packetevents.spigot)

    compileOnly(libs.protocollib)

    compileOnly(files("../../ansible/src/paper-plugins/BreweryX-3.6.3.jar"))
}
