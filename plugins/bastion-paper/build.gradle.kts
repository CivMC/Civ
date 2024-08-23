plugins {
    id("io.papermc.paperweight.userdev")
}

version = "3.0.1"

dependencies {
    paperweight {
        paperDevBundle("1.21.1-R0.1-SNAPSHOT")
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:citadel-paper"))
}
