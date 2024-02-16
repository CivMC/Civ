import net.civmc.civgradle.CivGradleExtension

plugins {
    id("net.civmc.civgradle") version "3.1.2" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "net.civmc.civgradle")

    configure<CivGradleExtension> {
        pluginName = project.property("pluginName") as String
    }

    repositories {
        mavenCentral()

        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://ci.frostcast.net/plugin/repository/everything")

        maven("https://jitpack.io")
    }
}
