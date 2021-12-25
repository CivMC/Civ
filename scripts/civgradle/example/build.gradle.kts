plugins {
    id("net.civmc.civgradle.plugin")
}

civGradle {
    civRepositories = listOf("CivModCore")

    paper {
        paperVersion = "1.18-R0.1-SNAPSHOT"
    }
}

