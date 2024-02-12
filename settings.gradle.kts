rootProject.name = "Civ"

plugins {
    id("com.gradle.enterprise") version "3.16.2"
}

include(":plugins:civspy-api")
include(":plugins:civspy-paper")
include(":plugins:civspy-bungee")
