plugins {
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

version = "2.0.1"

dependencies {
    paperweight {
        paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    }

    compileOnly("me.confuser:BarAPI:3.5")
}
