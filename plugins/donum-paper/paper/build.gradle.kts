plugins {
    id("net.civmc.civgradle")
}

civGradle {
    paper {
        pluginName = "Donum"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")

    compileOnly("net.civmc.civmodcore:paper:2.0.0-SNAPSHOT:dev-all")
    compileOnly("net.civmc.namelayer:paper:3.0.0-SNAPSHOT:dev")
}
