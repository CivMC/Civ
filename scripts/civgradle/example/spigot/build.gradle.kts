plugins {
    `java-library`
    `maven-publish`
    id("net.civmc.civgradle.plugin")
    id("io.papermc.paperweight.userdev") version "1.3.1"
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

civGradle {
    paper {
        pluginName = "ExamplePlugin"
    }
}

dependencies {
    paperDevBundle("1.18-R0.1-SNAPSHOT")
    compileOnly("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
}
