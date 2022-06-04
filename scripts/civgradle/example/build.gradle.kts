import net.civmc.civgradle.CivGradleExtension

plugins {
    id("net.civmc.civgradle.plugin") apply false
}

subprojects {
    apply(plugin = "net.civmc.civgradle.plugin")

    group = "net.cimc.exampleplugin"
    version = "1.0.0-SNAPSHOT"

    repositories {
        maven("https://repo.civmc.net/repository/maven-public")
    }

    configure<CivGradleExtension> {
        pluginName = "ExamplePlugin"
    }
}
