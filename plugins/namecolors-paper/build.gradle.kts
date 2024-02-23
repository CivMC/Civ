plugins {
    id("io.papermc.paperweight.userdev")
}

version = "2.0.0-SNAPSHOT"

dependencies {
    paperDevBundle("1.18.2-R0.1-SNAPSHOT")

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:civchat2-paper"))
    compileOnly("me.neznamy:tab-api:3.0.2")
}
