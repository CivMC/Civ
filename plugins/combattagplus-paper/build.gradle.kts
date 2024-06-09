plugins {
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

version = "2.0.1"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly("com.github.TownyAdvanced:towny:0.97.5.0")
    compileOnly("me.confuser:BarAPI:3.5")
}
