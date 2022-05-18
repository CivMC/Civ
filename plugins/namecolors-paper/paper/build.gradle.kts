plugins {
    `java-library`
    id("net.civmc.civgradle.plugin")
    id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
    paper {
        pluginName = "NameColors"
    }
}

dependencies {
    paperDevBundle("1.18.2-R0.1-SNAPSHOT")

    compileOnly("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
    compileOnly("net.civmc:namelayer-spigot:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:civchat2:2.0.0-SNAPSHOT:dev")
    compileOnly("me.neznamy:tab-api:3.0.2")
}
