plugins {
    alias(libs.plugins.paper.userdev)
}

version = "2.0.0-SNAPSHOT"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:banstick-paper"))
    compileOnly(project(":plugins:exilepearl-paper"))

    compileOnly(libs.bundles.nuvotifier)
}
