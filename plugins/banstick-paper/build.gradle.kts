plugins {
    id("io.papermc.paperweight.userdev")
    id("com.github.johnrengelman.shadow")
}

version = "2.0.1"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))

    implementation(libs.ipaddress)
    implementation(libs.jsoup)
}
