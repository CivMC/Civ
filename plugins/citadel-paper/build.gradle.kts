plugins {
    id("io.papermc.paperweight.userdev")
}

version = "5.2.4"

dependencies {
    paperweight {
        paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))

    compileOnly(files("../../ansible/src/paper-plugins/ProtocolLib.jar"))
}
