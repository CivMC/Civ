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

    testImplementation(libs.bundles.junit)
    testImplementation(libs.mockbukkit)
    testImplementation("io.papermc.paper:paper-api:${libs.versions.paper.get()}")
}

// https://docs.mockbukkit.org/docs/en/user_guide/advanced/paperweight
paperweight {
    addServerDependencyTo = configurations.named(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).map { setOf(it) }
}
