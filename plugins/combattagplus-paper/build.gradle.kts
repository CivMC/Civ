plugins {
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

version = "2.0.1"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(libs.barapi)
}
