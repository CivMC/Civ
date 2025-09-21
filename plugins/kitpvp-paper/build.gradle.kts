plugins {
    id("io.papermc.paperweight.userdev")
    id("com.gradleup.shadow")
}

version = "1.0.0"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:finale-paper"))
    compileOnly("net.luckperms:api:5.4")

    compileOnly(files("../../ansible/src/paper-plugins/BreweryX-3.4.10.jar"))
    compileOnly(libs.aswm.api)
    compileOnly("me.clip:placeholderapi:2.11.6")
}
