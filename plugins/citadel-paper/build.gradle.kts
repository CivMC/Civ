plugins {
    id("io.papermc.paperweight.userdev")
}

version = "5.2.4"

dependencies {
    paperweight {
        paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.9")
}
