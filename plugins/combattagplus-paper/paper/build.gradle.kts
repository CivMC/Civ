plugins {
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

dependencies {
    paperweight {
      paperDevBundle("1.18.2-R0.1-SNAPSHOT")
    }

    compileOnly("com.github.TownyAdvanced:towny:0.97.5.0")
    compileOnly("me.confuser:BarAPI:3.5")
}
