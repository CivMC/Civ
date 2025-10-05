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

    api("com.github.davidmoten:rtree2:0.9.3")
    api(libs.aikar.acf)
    api(libs.aikar.taskchain)
    api(libs.hikaricp)
    api(libs.commons.lang3)
    api(libs.commons.collections4)

    compileOnly(libs.fastutil)

    testImplementation(libs.bundles.junit)
}
