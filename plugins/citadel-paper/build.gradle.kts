plugins {
    id("io.papermc.paperweight.userdev")
}

version = "5.2.4"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.9")
}
