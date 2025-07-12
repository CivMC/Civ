plugins {
    id("io.papermc.paperweight.userdev")
    id("com.github.johnrengelman.shadow")
}

version = "3.0.8"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    api("com.github.davidmoten:rtree2:0.9.3")
    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:citadel-paper"))
}
