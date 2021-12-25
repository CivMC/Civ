rootProject.name = "civgradle"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

include(":example")
include(":example:spigot")

includeBuild("plugin")
