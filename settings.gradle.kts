rootProject.name = "Civ"

plugins {
    id("com.gradle.enterprise") version "3.16.2"
}

include(":ansible")

include(":plugins:banstick-paper")
include(":plugins:bastion-paper")
include(":plugins:castlegates-paper")
include(":plugins:citadel-paper")
include(":plugins:civchat2-paper")
include(":plugins:civmodcore-paper")
include(":plugins:combattagplus-paper")
include(":plugins:donum-paper")
include(":plugins:essenceglue-paper")
include(":plugins:exilepearl-paper")
include(":plugins:itemexchange-paper")
include(":plugins:jukealert-paper")
// TODO include(":plugins:namelayer-bungee")
include(":plugins:namelayer-paper")
include(":plugins:randomspawn-paper")
include(":plugins:realisticbiomes-paper")
include(":plugins:simpleadminhacks-paper")
include(":plugins:factorymod-paper")
include(":plugins:finale-paper")
include(":plugins:hiddenore-paper")
include(":plugins:railswitch-paper")
include(":plugins:civduties-paper")
include(":plugins:namecolors-paper")
include(":plugins:kirabukkitgateway-paper")
include(":plugins:heliodor-paper")
