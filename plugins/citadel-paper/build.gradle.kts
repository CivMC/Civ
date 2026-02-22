plugins {
    alias(libs.plugins.paper.userdev)
}

version = "5.2.4"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))

    compileOnly("com.github.retrooper:packetevents-spigot:2.11.2")
}
