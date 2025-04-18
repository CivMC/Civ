plugins {
    id("io.papermc.paperweight.userdev")
}

version = "2.0.0-SNAPSHOT"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:civchat2-paper"))

    compileOnly(files("../../ansible/src/paper-plugins/TAB v5.0.7.jar"))
}
