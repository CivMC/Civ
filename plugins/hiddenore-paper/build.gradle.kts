plugins {
    alias(libs.plugins.paper.userdev)
}

version = "2.0.0-SNAPSHOT"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }
}
