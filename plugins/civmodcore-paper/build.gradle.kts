plugins {
    id("io.papermc.paperweight.userdev")
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper")
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
