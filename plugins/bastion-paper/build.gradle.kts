plugins {
    id("io.papermc.paperweight.userdev")
}

version = "3.0.1"

dependencies {
    paperweight {
        paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:citadel-paper"))
}
