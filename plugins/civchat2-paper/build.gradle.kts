plugins {
    alias(libs.plugins.paper.userdev)
}

version = "2.2.2"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:banstick-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly("me.clip:placeholderapi:2.11.6")
}
