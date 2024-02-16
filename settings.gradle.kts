rootProject.name = "Civ"

plugins {
    id("com.gradle.enterprise") version "3.16.2"
}


include(":plugins:banstick-paper")
include(":plugins:bastion-paper")
include(":plugins:citadel-paper")
include(":plugins:civchat2-paper")
include(":plugins:civmodcore-paper")
include(":plugins:civspy-api")
include(":plugins:civspy-paper")
include(":plugins:civspy-bungee")
include(":plugins:combattagplus-paper")
// TODO include(":plugins:namelayer-bungee")
include(":plugins:namelayer-paper")
