plugins {
    `java-library`
    id("net.civmc.civgradle.plugin")
    id("io.papermc.paperweight.userdev") version "1.3.3"
}

civGradle {
    paper {
        pluginName = "CombatTagPlus"
    }
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    compileOnly("com.github.TownyAdvanced:towny:0.97.5.0")
    compileOnly("me.confuser:BarAPI:3.5")
}
