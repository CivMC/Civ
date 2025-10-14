plugins {
    alias(libs.plugins.paper.userdev)
    alias(libs.plugins.shadow)
}

version = "3.0.8"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:citadel-paper"))
}
