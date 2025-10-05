plugins {
    alias(libs.plugins.paper.userdev)
}

version = "1.0.0"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }
}
