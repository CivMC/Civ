rootProject.name = "combattagplus"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

// include("combattagpluscompat-api")
// include("combattagplushook")
include("combattagplus-spigot")
