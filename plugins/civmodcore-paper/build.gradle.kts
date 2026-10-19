plugins {
    alias(libs.plugins.paper.userdev)
    alias(libs.plugins.shadow)
    alias(libs.plugins.runpaper)
}

version = "3.0.6"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    api(libs.rtree2)
    api(libs.aikar.acf)
    api(libs.aikar.taskchain)
    api(libs.hikaricp)
    api(libs.commons.lang3)
    api(libs.commons.collections4)

    compileOnly(libs.fastutil)
}

tasks {
    test.configure {
        enabled = false // Disabled until the tests are fixed
    }
}
