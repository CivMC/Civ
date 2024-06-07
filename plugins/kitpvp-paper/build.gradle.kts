plugins {
    id("io.papermc.paperweight.userdev")
    id("com.github.johnrengelman.shadow")
}

version = "1.0.0"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:finale-paper"))
    compileOnly("com.github.Jsinco:BreweryX:3.3.2")
    compileOnly("com.infernalsuite.aswm:api:3.0.0-SNAPSHOT")
}
