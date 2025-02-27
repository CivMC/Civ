plugins {
    id("io.papermc.paperweight.userdev")
}

version = "1.0.0"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }
}
