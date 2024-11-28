plugins {
    id("io.papermc.paperweight.userdev")
}

version = "1.0.0"

dependencies {
    paperweight {
        paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    }

    compileOnly(project(":plugins:civmodcore-paper"))
}
