import net.civmc.civgradle.common.util.civRepo

plugins {
    id("net.civmc.civgradle.plugin")
}

subprojects {
    apply(plugin = "net.civmc.civgradle.plugin")

    group = "net.cimc.exampleplugin"
    version = "1.0.0-SNAPSHOT"

    repositories {
        civRepo("CivMC/CivModCore")
    }
}
